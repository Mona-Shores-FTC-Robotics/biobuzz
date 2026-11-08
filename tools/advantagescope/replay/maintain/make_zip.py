"""Zips asset folders reproducibly: each folder at the zip's top level, entries sorted, every timestamp fixed.

    python3 make_zip.py OUT.zip FOLDER [FOLDER ...]
"""
import os
import sys
import zipfile

FIXED = (2026, 1, 1, 0, 0, 0)


def main(out, folders):
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as z:
        for folder in folders:
            base = os.path.basename(os.path.normpath(folder))
            for root, dirs, files in os.walk(folder):
                dirs.sort()
                for name in sorted(files):
                    if name.startswith(".") or name.endswith(".csv"):
                        continue
                    path = os.path.join(root, name)
                    arc = base + "/" + os.path.relpath(path, folder).replace(os.sep, "/")
                    info = zipfile.ZipInfo(arc, FIXED)
                    info.compress_type = zipfile.ZIP_DEFLATED
                    info.external_attr = 0o644 << 16
                    with open(path, "rb") as f:
                        z.writestr(info, f.read(), compresslevel=9)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2:])
