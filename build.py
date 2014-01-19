import zipfile
import os
import shutil

z = zipfile.ZipFile("C:\\Users\\Simon\\Documents\\Robotics\\RText\\plugins\\SRPlugin.jar", "w")

def search(root, d):
    c = os.path.join(root, d)
    for f in os.listdir(c):
        ab = os.path.join(c, f)
        rel = os.path.join(d, f)
        if os.path.isdir(ab):
            search(root, rel)
        else:
            z.write(ab, arcname=rel)

search("C:\\Users\\Simon\\workspace\\SRPlugin\\bin", "")

manifest="""Manifest-Version: 1.0
Ant-Version: Apache Ant 1.8.2
Created-By: 1.5.0_22-b03 (Sun Microsystems Inc.)
Fife-Plugin-Class: com.simon816.rtext.srplugin.Plugin
Fife-Plugin-Load-Priority: highest

"""

z.writestr("META-INF/MANIFEST.MF", manifest)
z.close()

shutil.copyfile(z.filename, "C:\\Users\\Simon\\workspace\\RText\\dist\\Plugins\\SRPlugin.jar")
