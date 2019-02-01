import argparse

parser = argparse.ArgumentParser(description='Hiding text in images using '
                                             'Least Significant Bit '
                                             'Stenography')
text_group = parser.add_mutually_exclusive_group()
text_group.add_argument('-f', '--file', type=str,
                        help='Filename of text to hide')
text_group.add_argument('-m', '--message', type=str, help='Message to hide.')
parser.add_argument('-r', '--retrieve', action='store_true', default=False,
                    help='Retrieve a message from a given image')
parser.add_argument('image', nargs='?', help='Filename of image to use')
