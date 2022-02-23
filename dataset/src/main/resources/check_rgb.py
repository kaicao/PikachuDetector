# photos must be RGB otherwise TensorFlow will fail executing
from PIL import Image
import os

def main():
  path = 'raw/pikachu/'
  for file in os.listdir(path):
     extension = file.split('.')[-1]
     if extension == "DS_Store":
       continue
     fileLoc = path+file
     img = Image.open(fileLoc)
     if img.mode != 'RGB':
       print(file+', '+img.mode)

main()