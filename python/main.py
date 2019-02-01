from cryptor import Cryptor
from parser import parser

if __name__ == "__main__":
    args = parser.parse_args()
    Cryptor.handle(args)
