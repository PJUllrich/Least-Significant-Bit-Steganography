from lsb import LSB
from parser import parser

if __name__ == "__main__":
    args = parser.parse_args()
    LSB.handle(args)
