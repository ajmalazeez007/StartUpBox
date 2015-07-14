import Tkinter as Tk
from skimage.measure import structural_similarity as ssim
import matplotlib.pyplot as plt
import numpy as np
import cv2
import urllib


class App(object):
    def __init__(self,):
        self.root = Tk.Tk()
       
        self.root.wm_title("Compare image")
        self.url1 = Tk.StringVar()
        self.url2 = Tk.StringVar()
        self.label = Tk.Label(self.root, text="Image 1:")
        self.label.pack()
        Tk.Entry(self.root, textvariable=self.url1).pack()
        self.label = Tk.Label(self.root, text="Image 2:")
        self.label.pack()
        self.weight_in_kg = Tk.StringVar()
        Tk.Entry(self.root, textvariable=self.url2).pack()

        self.buttontext = Tk.StringVar()
        self.buttontext.set("Compare")
        Tk.Button(self.root,
                  textvariable=self.buttontext,
                  command=self.clicked1).pack()

        self.label = Tk.Label(self.root, text="")
        self.label.pack()

        self.root.mainloop()

    def clicked1(self):
        url1 = self.url1.get()
        url2 = self.url2.get()
        self.label.configure(text=url1)
        resource = urllib.urlopen(url1)
        output = open("1.png","wb")
        
        output.write(resource.read())
        output.close()
        
        resource = urllib.urlopen(url2)
        output = open("2.png","wb")
        output.write(resource.read())
        output.close()
        
        img1 = cv2.imread("1.png")
        img2 = cv2.imread("2.png")
        
        img11 = cv2.cvtColor(img1, cv2.COLOR_BGR2GRAY)
        img12 = cv2.cvtColor(img2, cv2.COLOR_BGR2GRAY)
        
        imageA = cv2.resize(img11, (100, 100)) 
        imageB = cv2.resize(img12, (100, 100)) 
        
        s = ssim(imageA, imageB)
    
        title ="Comparing"   
        fig = plt.figure(title)
        if s<0:
            s=0
        plt.suptitle("Percentage : %.2f " % (s*100))
    
        # show first image
        ax = fig.add_subplot(1, 2, 1)
        plt.imshow(imageA, cmap = plt.cm.gray)
        plt.axis("off")
    
        # show the second image
        ax = fig.add_subplot(1, 2, 2)
        plt.imshow(imageB, cmap = plt.cm.gray)
        plt.axis("off")
    
        # show the images
        plt.show()
        
    def button_click(self, e):
        pass

App()


