# hash-of-file-by-sha256

***

## Description
This project implements file hashing using the sha-256 algorithm. All<br>
functionality is represented only FileConverterToHashBySha256 class. In<br>
order to carry out hashing, you need to create an object of this class and<br>
simply pass the absolute path to the desired file to the generateOur() method<br>
of this object. The result will be returned as a BigInteger.<br>

***

## Steps of hashing
1. When an object of class FileConvertorToHashBySha256 is created, the<br> 
following steps are taken:<br>
+ The method generatorOfPrimes() generate first 64 prime numbers are  and<br>
stored in the field PRIMES;<br>
+ Method hashValuesInit() fill the array hashValues[] hashes using first 8<br>
prime numbers (the first 32 bits of fractional parts of their square roots);<br>
+ Method roundedConstantsInit() fill the array ROUNED_CONSTANTS[] integer<br>
numbers using first 64 prime numbers (the first 32 bits of fractional parts of<br>
their cube roots);<br>
2. Method readBytesFromFile() reads bytes from the file into byte array data[],<br>
with the final number of bits always a multiple of 512, this is done by adding<br>
zero bits to the end of the array if necessary;<br><br>
3. The method preprocessing() writes the length of all data (bits) to the last<br>
64 bits of byte array data[];<br><br>
4. Main cycle. The following steps will be performed for each 512-bit chunk of<br>
the data[] array. On each iteration of the loop, the hash values in the<br>
hashValues[] array will be changed:<br>
* The method createQueueMessages() creates a message queue (32 bit array of<br> 
64 elements) encoded in a special way based on a piece of array data[];<br>
+ The method compressionCycle() writes the hash values to the tempContainers[]<br>
array of integers. Then these values are changed in a special way;<br>
+ The method changeHashValues() modifies the hash values in the hashValues[]<br>
array by adding to these hashes to the elements of the tempContainers[] array<br>
modulo 2^32;<br>
5. Then occur a join together the modified hashes of the hashValues[] array<br>
into a BigInteger variable in hexadecimal notation.<br>

