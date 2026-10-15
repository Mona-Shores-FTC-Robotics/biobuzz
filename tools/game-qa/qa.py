#!/usr/bin/env python3
"""Print the official FTC Game Q&A board for BIOBUZZ as plain text.

The board (https://game-qa.firstinspires.org/boards/2027/FTC/QA) is a JavaScript app, but the
page embeds every question and its answer in an `Object.assign(window, {...})` call, so one
fetch is enough. No login, no dependencies beyond the standard library.

    python3 tools/game-qa/qa.py            # every question, oldest first
    python3 tools/game-qa/qa.py --json     # the raw board JSON
    python3 tools/game-qa/qa.py --since 20 # questions numbered 20 and up

Re-run it before relying on `doc/game/qa-review.md`: a question marked ACCEPTED there may have
been answered since.
"""
import argparse
import datetime
import html
import json
import re
import sys
import urllib.request

URL = "https://game-qa.firstinspires.org/boards/2027/FTC/QA"


def fetch_board(url=URL):
    with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": "biobuzz-qa/1"})) as r:
        page = r.read().decode("utf-8")
    m = re.search(r"Object\.assign\(window,\s*(\{.*?\})\);?\s*\n", page, re.S)
    if not m:
        sys.exit("The page no longer embeds window.questions; open it in a browser instead.")
    return json.loads(m.group(1))


def text(fragment):
    s = html.unescape(fragment or "")
    s = re.sub(r"</p>|<br ?/?>|</li>", "\n", s)
    s = re.sub(r"<li>", "- ", s)
    s = re.sub(r"<[^>]+>", "", s)
    return re.sub(r"\n\s*\n+", "\n", s).strip()


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--json", action="store_true", help="print the raw board JSON")
    ap.add_argument("--since", type=int, default=0, help="first question number to print")
    args = ap.parse_args()
    board = fetch_board()
    if args.json:
        json.dump(board, sys.stdout, indent=1)
        return
    questions = sorted(board["questions"], key=lambda q: q["number"])
    answered = sum(1 for q in questions if q.get("answer"))
    print(f"{len(questions)} questions, {answered} answered, fetched {datetime.date.today()}\n")
    for q in questions:
        if q["number"] < args.since:
            continue
        asked = datetime.datetime.fromtimestamp(q["createdAt"] / 1000, datetime.timezone.utc).date()
        print(f"==== Q{q['number']} [{q['status']}] rule {q.get('rule')} §{q.get('topic')} ({asked}): {q['title']}")
        print("Q: " + text(q["content"]))
        a = q.get("answer")
        print("A: " + (text(a["content"]) if a else "(no answer yet)"))
        print()


if __name__ == "__main__":
    main()
