Proper usage of this application (the executable jar version) is:

java -jar joiner.jar firstFileName [secondFileName [-numbersOnly | -normalizeForLength]

If a second filename is not specified, the system uses the first filename twice.

Use the -numbersOnly option for output in the following form:
length1,length2,difference
for each element in the join.

The -normalizeForLength option will modify the similarity value 
by dividing it by the greater of the two string lengths.