package cs107;

/**
 * Utility class to manipulate arrays.
 * @apiNote First Task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.3
 * @since 1.0
 */
public final class ArrayUtils {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private ArrayUtils(){}

    // ==================================================================================
    // =========================== ARRAY EQUALITY METHODS ===============================
    // ==================================================================================

    /**
     * Check if the content of both arrays is the same
     * @param a1 (byte[]) - First array
     * @param a2 (byte[]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[] a1, byte[] a2){
        for (int i = 0; i < a1.length; i++) {
            if(a1[i] != a2[i])
                return false;
        }

        return true;
    }

    /**
     * Check if the content of both arrays is the same
     * @param a1 (byte[][]) - First array
     * @param a2 (byte[][]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[][] a1, byte[][] a2){
        for (int i = 0; i < a1.length; i++) {
            if(!equals(a1[i], a2[i])){
                return false;
            }
        }

        return true;
    }

    // ==================================================================================
    // ============================ ARRAY WRAPPING METHODS ==============================
    // ==================================================================================

    /**
     * Wrap the given value in an array
     * @param value (byte) - value to wrap
     * @return (byte[]) - array with one element (value)
     */
    public static byte[] wrap(byte value){
        byte[] array = new byte[1];
        array[0] = value;

        return array;
    }

    // ==================================================================================
    // ========================== INTEGER MANIPULATION METHODS ==========================
    // ==================================================================================

    /**
     * Create an Integer using the given array. The input needs to be considered
     * as "Big Endian"
     * (See handout for the definition of "Big Endian")
     * @param bytes (byte[]) - Array of 4 bytes
     * @return (int) - Integer representation of the array
     * @throws AssertionError if the input is null or the input's length is different from 4
     */
    public static int toInt(byte[] bytes){
        assert bytes.length == 4;
        assert bytes != null;

        int nb = 0;
        for (int i=0 ; i<bytes.length ; ++i){
            nb = (nb << 8) | (bytes[i] & 0xFF);
        }

        return nb;
    }

    /**
     * Separate the Integer (word) to 4 bytes. The Memory layout of this integer is "Big Endian"
     * (See handout for the definition of "Big Endian")
     * @param value (int) - The integer
     * @return (byte[]) - Big Endian representation of the integer
     */
    public static byte[] fromInt(int value){
        byte[] array = new byte[4];
        for (int i=0 ; i<array.length ; ++i){
            array[i] = (byte) (value >> ((array.length-1-i)*8));
        }

        return array;
    }

    // ==================================================================================
    // ========================== ARRAY CONCATENATION METHODS ===========================
    // ==================================================================================

    /**
     * Concatenate a given sequence of bytes and stores them in an array
     * @param bytes (byte ...) - Sequence of bytes to store in the array
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     */
    public static byte[] concat(byte ... bytes){
        assert bytes != null;

        return bytes;
    }

    /**
     * Concatenate a given sequence of arrays into one array
     * @param tabs (byte[] ...) - Sequence of arrays
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null.
     */
    public static byte[] concat(byte[] ... tabs){
        assert tabs != null;

        int size = 0;
        for (byte[] elem : tabs){
            size += elem.length;
        }

        byte[] tab = new byte[size];
        
        int diff = size;
        for (byte[] elem : tabs){
            assert elem != null;
            for (byte bytes : elem){
                tab[size-diff] = bytes;
                --diff;
            }
        }

        return tab;
    }

    // ==================================================================================
    // =========================== ARRAY EXTRACTION METHODS =============================
    // ==================================================================================

    /**
     * Extract an array from another array
     * @param input (byte[]) - Array to extract from
     * @param start (int) - Index in the input array to start the extract from
     * @param length (int) - The number of bytes to extract
     * @return (byte[]) - The extracted array
     * @throws AssertionError if the input is null or start and length are invalid.
     * start + length should also be smaller than the input's length
     */
    public static byte[] extract(byte[] input, int start, int length){
        assert input != null;
        assert start >= 0 && start <= input.length;
        assert length >= 0;
        assert start+length <= input.length;

        byte[] tab = new byte[length];
        for (int i=start ; i<start+length ; ++i){
            tab[i-start] = input[i];
        }

        return tab;
    }

    /**
     * Create a partition of the input array.
     * (See handout for more information on how this method works)
     * @param input (byte[]) - The original array
     * @param sizes (int ...) - Sizes of the partitions
     * @return (byte[][]) - Array of input's partitions.
     * The order of the partition is the same as the order in sizes
     * @throws AssertionError if one of the parameters is null
     * or the sum of the elements in sizes is different from the input's length
     */
    public static byte[][] partition(byte[] input, int ... sizes) {
        assert input != null;
        assert sizes != null;
        int somme = 0;
        for (int elem : sizes){
            somme+=elem;
        }
        assert somme == input.length;

        int start = 0;

        byte[][] tab = new byte[sizes.length][];

        for (int i=0 ; i<sizes.length ; ++i){
            tab[i] = extract(input, start, sizes[i]);
            start += sizes[i];
        }

        return tab;
    }

    // ==================================================================================
    // ============================== ARRAY FORMATTING METHODS ==========================
    // ==================================================================================

    /**
     * Format a 2-dim integer array
     * where each dimension is a direction in the image to
     * a 2-dim byte array where the first dimension is the pixel
     * and the second dimension is the channel.
     * See handouts for more information on the format.
     * @param input (int[][]) - image data
     * @return (byte [][]) - formatted image data
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null
     */
    public static byte[][] imageToChannels(int[][] input){
        assert input != null;
        int[] prevelem = input[0];
        for (int[] elem : input){
            assert elem != null;
            assert elem.length == prevelem.length;
            prevelem = elem;
        }

        
        byte[][] tab = new byte[input[0].length*input.length][4];

        for(int i=0 ; i<input.length ; ++i){
        int[] pixels = input[i];
            for(int j=0 ; j<pixels.length ; ++j){
                byte[] comps = fromInt(pixels[j]);
                for(int k=0 ; k<4 ; ++k){    
                    switch(k){
                        case 0 : 
                        tab[j+i*pixels.length][QOISpecification.a] = comps[k]; break;
                        case 1 : 
                        tab[j+i*pixels.length][QOISpecification.r] = comps[k]; break;
                        case 2 : 
                        tab[j+i*pixels.length][QOISpecification.g] = comps[k]; break;
                        case 3 : 
                        tab[j+i*pixels.length][QOISpecification.b] = comps[k]; break;
                    }                    
                }
            } 
        }

        return tab;
    }

 
    /**
     * Format a 2-dim byte array where the first dimension is the pixel
     * and the second is the channel to a 2-dim int array where the first
     * dimension is the height and the second is the width
     * @param input (byte[][]) : linear representation of the image
     * @param height (int) - Height of the resulting image
     * @param width (int) - Width of the resulting image
     * @return (int[][]) - the image data
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null
     * or input's length differs from width * height
     * or height is invalid
     * or width is invalid
     */
    public static int[][] channelsToImage(byte[][] input, int height, int width){
        assert input != null;
        for(byte[] elem : input){
            assert elem != null;
            assert elem.length == 4;
        }
        assert input.length == height*width;

        int[][] tab = new int[height][width];
        byte[][] pixels = new byte[input.length][4];

        //Transform rgb-a to a-rgb
        for(int i=0 ; i<pixels.length ; ++i){
            pixels[i][0] = input[i][QOISpecification.a];
            pixels[i][1] = input[i][QOISpecification.r];
            pixels[i][2] = input[i][QOISpecification.g];
            pixels[i][3] = input[i][QOISpecification.b];
        }

        //Fills tab with transformed pixels and to Int
        for(int i=0 ; i<height ; ++i){
            for(int j=0 ; j<width ; ++j){
                tab[i][j] = toInt(pixels[j+i*width]);
            }
        }

        return tab;
    }

    // ==================================================================================
    // ============================ STUDENT'S ADDED METHODS =============================
    // ==================================================================================

    /**
     * Casts a byte[] to a Byte[]
     */
    public static Byte[] cast(byte[] bytes){
        Byte[] tab = new Byte[bytes.length];
        for(int i=0 ; i<bytes.length ; ++i){
            tab[i] = bytes[i];
        }
        return tab;
    }

    /**
     * Casts a Byte[] to a byte[]
     */
    public static byte[] cast(Byte[] bytes){
        byte[] tab = new byte[bytes.length];
        for(int i=0 ; i<bytes.length ; ++i){
            tab[i] = bytes[i];
        }
        return tab;
    }

}