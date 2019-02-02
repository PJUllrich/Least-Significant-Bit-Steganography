import numpy as np
from PIL import Image


class LSB:
    """
    Implements encryption and decryption functionality for Least Significant
    Bit Stenography.

    For encryption, the color components (Red-Green-Blue) of every pixel of
    a given image are taken and the last 2 bits of every component are
    replaced with 2 bits of a character of a given text. For optimal
    performance, Numpy arrays and bitwise operations are heavily used.

    For decryption, the last 2 bits of every color component of every pixel
    are retrieved and concatenated back to a hidden text.

    Given texts are encoded using UTF-8
    """
    FILENAME_OUTPUT = 'output.png'

    @staticmethod
    def handle(args):
        img = Image.open(args.image)

        if args.retrieve:
            LSB.retrieve(img)
        else:
            text = LSB.get_text(args)
            LSB.insert(img, text)

    @staticmethod
    def retrieve(img: Image):
        """Retrieves a hidden text from a given Image"""
        arr = np.array(img).astype(np.uint8).reshape(-1)
        bits = arr & 3
        text = LSB.bits_to_str(bits)
        print(text)

    @staticmethod
    def insert(img: Image, msg: str):
        """Inserts a given text into a given Image and saves the output"""
        arr = np.array(img).astype(np.uint8)
        flat = arr.reshape(-1).copy()
        bits = LSB.str_to_bits(msg)

        length = len(bits)
        flat[:length] = (flat[:length] & 252) + bits
        stego = flat.reshape(arr.shape)
        Image.fromarray(stego).save(LSB.FILENAME_OUTPUT)

    @staticmethod
    def get_text(args):
        """Returns a text given as an argument or reads in a given file"""
        if args.message is not None:
            return args.message

        return open(args.file, encoding='utf-8').read()

    @staticmethod
    def bits_to_str(bits: np.ndarray):
        """Sums up blocks of 4 and returns a char representation of the sum"""
        chars = []
        for i in np.arange(0, len(bits), 4):
            val = LSB.bits_to_int(bits[i:i + 4])
            if val == 255:
                return bytes(chars).decode('utf-8')

            chars.append(val)

        raise ValueError('Could not find end block during decryption.')

    @staticmethod
    def bits_to_int(bits: np.ndarray) -> int:
        """
        Shifts 2-bit pairs back into their position of an 8-bit string and
        sums up their integer values.

        Example:
            Input = [1, 2, 2, 0] = [b'01', b'10', b'10', b'00']
            Element 1 = b'01' << 6 = b'01000000' = 64
            Element 2 = b'10' << 4 = b'00100000' = 32
            Element 3 = b'10' << 2 = b'00001000' = 8
            Element 4 = b'00' << 0 = b'00000000' = 0
            Sum of all elements = 64 + 32 + 8 + 0 = 104
            Returns 104

        :param bits: array of 4 2-bit elements as int
        :return: sum of shifted bits as int
        """
        ints = [(bits[i] << op) for i, op in enumerate(range(6, -1, -2))]
        return sum(ints)

    @staticmethod
    def str_to_bits(text: str) -> np.ndarray:
        """
        Converts a string (text) to a 1d Numpy bit array.

        The string is first converted to bytes based on UTF-8.
        Every byte is then converted into 4 2-bit elements.
        All 2-bit elements are stored in a Numpy array.

        Example
            'foo'.encode('utf-8') = [102, 111, 111] (as ASCII numbers)
            [102, 111, 111] = ['01100110', '01101111', '01101111']
            ['01100110', ...] = ['01', '10', '01', '10', ...]
            ['01', '10', '01', '10', ...] = [1, 2, 1, 2, ...]
            Returns [1, 2, 1, 2, ...]

        :param text: the string to be converted
        :return: Numpy bit array
        """
        msg_bytes = text.encode('utf-8')
        bits = []
        for byte in msg_bytes:
            bits.extend([(byte >> i) & 3 for i in range(6, -1, -2)])
        bits.extend([3, 3, 3, 3])
        return np.array(bits)
