from PIL import Image
import numpy as np


class Cryptor:

    @staticmethod
    def handle(args):
        img = Image.open(args.image)

        if args.retrieve:
            Cryptor.retrieve(img)
        else:
            Cryptor.insert()

    @staticmethod
    def retrieve(img: Image):
        arr = np.array(img)
        bits = np.unpackbits(arr, axis=2)
        print(bits[:1, :1])


    @staticmethod
    def insert():
        pass
