"""page/render_v10.html: the v10 parts in three.js, flat and tripped, beside the FLOWER's posts; shot_v10.js screenshots it."""
import base64, os, sys
M = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..") if "page" in os.path.abspath(__file__) else "/home/user/biobuzz/cad/modules/flower-stick-proto"
b = lambda n: base64.b64encode(open(os.path.join(M, "stl", n + ".stl"), "rb").read()).decode()
html = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "render_v10.template.html")).read()
html = html.replace("__BODY__", b("v10_body")).replace("__NOSE__", b("v10_nose")).replace("__PIN__", b("v10_pin"))
open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "render_v10.html"), "w").write(html); print("render_v10.html")
