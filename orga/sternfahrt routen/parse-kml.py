from __future__ import print_function
import glob
import os
from xml.dom import minidom
os.chdir("./kml/")

for file in glob.glob("*.*"):
    openedFile = open(file,'r')
    xmldoc = minidom.parse(openedFile)
    coordString = xmldoc.getElementsByTagName("coordinates")[-1].toxml() 
    print("\n".join(coordString.split( " ")))
    openedFile.close()



