"""Draws the countdown images open-match.ps1 burns into each clip (marker/0-05.png ... 0-02.png).

AdvantageScope syncs a video by reading the match timer off it (Tesseract, at fixed spots on screen) and
locking where it changes from 0:04 to 0:03; FTC's stream overlay does not put its timer there, so the clip
carries its own. These are those digits: white on black, sized for the 2024/2025 lower-banner spot at
1280x720. Run once; the PNGs are committed.   python3 make_marker.py OUT_DIR
"""
import os
import sys

from PIL import Image, ImageDraw, ImageFont

W, H = 120, 52
FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"


def main(out):
    font = ImageFont.truetype(FONT, 40)
    for n in (5, 4, 3, 2):
        text = f"0:0{n}"
        img = Image.new("RGB", (W, H), "black")
        d = ImageDraw.Draw(img)
        box = d.textbbox((0, 0), text, font=font)
        d.text(((W - (box[2] - box[0])) / 2 - box[0], (H - (box[3] - box[1])) / 2 - box[1]), text, fill="white", font=font)
        img.save(os.path.join(out, f"0-0{n}.png"), optimize=True)


if __name__ == "__main__":
    main(sys.argv[1])
