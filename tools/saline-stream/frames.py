#!/usr/bin/env python3
"""Decode a window of a clip at a given fps into a uint8 array (n, h, w, 3) with ffmpeg."""
import subprocess, numpy as np
def read(clip, t0, t1, fps=20, w=1280, h=720):
    cmd = ['ffmpeg', '-nostdin', '-v', 'error', '-ss', f'{t0:.3f}', '-t', f'{t1-t0:.3f}', '-i', clip,
           '-vf', f'fps={fps}', '-f', 'rawvideo', '-pix_fmt', 'rgb24', '-']
    raw = subprocess.run(cmd, capture_output=True, check=True).stdout
    n = len(raw) // (w*h*3); a = np.frombuffer(raw[:n*w*h*3], np.uint8).reshape(n, h, w, 3)
    return a, t0 + (np.arange(n) + 0.5) / fps
