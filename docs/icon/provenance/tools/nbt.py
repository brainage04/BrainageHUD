"""Typed NBT reader/writer (stdlib only) for Minecraft level.dat surgery.

Every tag is a `Tag` carrying its original type id, so a read -> edit -> write
round trip re-serializes untouched values with their original tag types; only the
values that are deliberately edited change.
"""

from __future__ import annotations

import gzip
import io
import struct

TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12


class Tag:
    """One NBT tag: `type` (id), `value` (payload) and, for lists, `child`."""

    __slots__ = ("type", "value", "child")

    def __init__(self, type: int, value, child: int | None = None):
        self.type = type
        self.value = value
        self.child = child

    def __repr__(self) -> str:  # pragma: no cover - debugging aid
        return f"Tag({self.type}, {self.value!r})"


def _read(r, fmt):
    size = struct.calcsize(fmt)
    out = r.read(size)
    if len(out) != size:
        raise EOFError("truncated NBT")
    return struct.unpack(fmt, out)[0]


def _read_string(r) -> str:
    return r.read(_read(r, ">H")).decode("utf-8")


def _read_payload(r, tag_id: int) -> Tag:
    if tag_id == TAG_BYTE:
        return Tag(tag_id, _read(r, ">b"))
    if tag_id == TAG_SHORT:
        return Tag(tag_id, _read(r, ">h"))
    if tag_id == TAG_INT:
        return Tag(tag_id, _read(r, ">i"))
    if tag_id == TAG_LONG:
        return Tag(tag_id, _read(r, ">q"))
    if tag_id == TAG_FLOAT:
        return Tag(tag_id, _read(r, ">f"))
    if tag_id == TAG_DOUBLE:
        return Tag(tag_id, _read(r, ">d"))
    if tag_id == TAG_BYTE_ARRAY:
        return Tag(tag_id, r.read(_read(r, ">i")))
    if tag_id == TAG_STRING:
        return Tag(tag_id, _read_string(r))
    if tag_id == TAG_LIST:
        child = _read(r, ">B")
        length = _read(r, ">i")
        return Tag(tag_id, [_read_payload(r, child) for _ in range(length)], child)
    if tag_id == TAG_COMPOUND:
        out: dict[str, Tag] = {}
        while True:
            child = _read(r, ">B")
            if child == TAG_END:
                return Tag(tag_id, out)
            name = _read_string(r)
            out[name] = _read_payload(r, child)
    if tag_id == TAG_INT_ARRAY:
        count = _read(r, ">i")
        return Tag(tag_id, [_read(r, ">i") for _ in range(count)])
    if tag_id == TAG_LONG_ARRAY:
        count = _read(r, ">i")
        return Tag(tag_id, [_read(r, ">q") for _ in range(count)])
    raise ValueError(f"unknown tag id {tag_id}")


def _write_tag(out: bytearray, tag: Tag) -> None:
    out += struct.pack(">B", tag.type)
    _write_payload(out, tag)


def _write_payload(out: bytearray, tag: Tag) -> None:
    tag_id, value = tag.type, tag.value
    if tag_id == TAG_BYTE:
        out += struct.pack(">b", value)
    elif tag_id == TAG_SHORT:
        out += struct.pack(">h", value)
    elif tag_id == TAG_INT:
        out += struct.pack(">i", value)
    elif tag_id == TAG_LONG:
        out += struct.pack(">q", value)
    elif tag_id == TAG_FLOAT:
        out += struct.pack(">f", value)
    elif tag_id == TAG_DOUBLE:
        out += struct.pack(">d", value)
    elif tag_id == TAG_BYTE_ARRAY:
        out += struct.pack(">i", len(value)) + value
    elif tag_id == TAG_STRING:
        raw = value.encode("utf-8")
        out += struct.pack(">H", len(raw)) + raw
    elif tag_id == TAG_LIST:
        out += struct.pack(">B", tag.child if tag.child is not None else TAG_END)
        out += struct.pack(">i", len(value))
        for item in value:
            _write_payload(out, item)
    elif tag_id == TAG_COMPOUND:
        for name, child in value.items():
            out += struct.pack(">B", child.type)
            raw = name.encode("utf-8")
            out += struct.pack(">H", len(raw)) + raw
            _write_payload(out, child)
        out += b"\x00"
    elif tag_id == TAG_INT_ARRAY:
        out += struct.pack(">i", len(value))
        for item in value:
            out += struct.pack(">i", item)
    elif tag_id == TAG_LONG_ARRAY:
        out += struct.pack(">i", len(value))
        for item in value:
            out += struct.pack(">q", item)
    else:
        raise ValueError(f"unknown tag id {tag_id}")


def read_file(path) -> tuple[str, Tag]:
    """Return (root name, root Tag) of an NBT file (gzip auto-detected)."""
    with open(path, "rb") as handle:
        return read_bytes(handle.read())


def read_bytes(raw: bytes) -> tuple[str, Tag]:
    if raw[:2] == b"\x1f\x8b":
        raw = gzip.decompress(raw)
    r = io.BytesIO(raw)
    tag_id = _read(r, ">B")
    name = _read_string(r)
    return name, _read_payload(r, tag_id)


def write_file(path, name: str, tag: Tag, gzipped: bool = True) -> None:
    raw = write_bytes(name, tag)
    if gzipped:
        with gzip.open(path, "wb") as handle:
            handle.write(raw)
    else:
        with open(path, "wb") as handle:
            handle.write(raw)


def write_bytes(name: str, tag: Tag) -> bytes:
    out = bytearray()
    out += struct.pack(">B", tag.type)
    raw = name.encode("utf-8")
    out += struct.pack(">H", len(raw)) + raw
    _write_payload(out, tag)
    return bytes(out)


# -- convenience helpers -----------------------------------------------------

def get(root: Tag, *path) -> Tag | None:
    """Follow a path of compound keys / list indices, returning the Tag or None."""
    node = root
    for key in path:
        if node is None:
            return None
        if isinstance(key, int):
            if node.type != TAG_LIST or key >= len(node.value):
                return None
            node = node.value[key]
        else:
            if node.type != TAG_COMPOUND or key not in node.value:
                return None
            node = node.value[key]
    return node


def string_list(items: list[str]) -> Tag:
    return Tag(TAG_LIST, [Tag(TAG_STRING, item) for item in items], TAG_STRING)
