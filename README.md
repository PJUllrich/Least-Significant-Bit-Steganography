# Least Significant Bit Steganography

This video of Computerphile (https://www.youtube.com/watch?v=TWEXCYQKyDc) inspired me to write my own
little program to add a message to the least significant bits of an image (i.e. the last 2 bits of the binary values of the color components of a pixel).

This project does only support input text files in **UTF-8** format and its output pictures are in a .png format. 
It is small and not fault-proof, but it works! (kind of). 

simply run it with the parameters:          *-f exampleTextShort.txt example.png*<br>
And then retrieve that message again with:  *-r output.png*

If you feel like it you can contribute by: <br>

- Adding support for different text file formats (e.g. UTF-16, ASCII)
- Tell me how to add syntactic sugar to my code (highly appreciated!)
- Let the program add the message not only to the pixels from top left to bottom right, but by using different patterns (e.g. sinus-waves)
- Add some statistical output for the distribution of color component values. This is important as one can easily tell whether an image contains a message by looking at that distribution (somehow, dunno how exactly).
- **Leave any other king of feedback, suggestions, praise, or criticism. **
