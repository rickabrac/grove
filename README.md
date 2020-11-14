## Synopsis

Java implementation of find_stores coding challenge

## System Requirements 

JDK 8, Ant 1.9, Bourne shell 

## Installation

• Open a terminal and change to the cloned subdirectory 

• Enter 'ant' at the command prompt to compile the program

• On unix systems, type 'chmod +x find_store' (if find_store script is not executable)

• find_store is a bourne shell wrapper for java command required to run executable

## Assumptions

• The 'java' and 'ant' commands must be in the user's execution path.

## Caveats

• I assumed the geolocation API I selected (https://api.positionstack.com) would be accurate. 
  It returns a JSON array of potential matches. In ad hoc testing, I noticed that it sometimes
  returns bogus results.  To improve accuracy, I would try a different geolocation API.

• If there is a connection failure, the default timeout is too long (~20 seconds).

• No unit or regression tests :(

