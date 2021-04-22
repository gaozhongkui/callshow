package com.epiphany.jextractor.cipher;


public interface CipherFunction {

    char[] apply(char[] array, String argument);
}
