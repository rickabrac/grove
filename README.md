## Synopsis

Java implementation of "Grove Coding Challenge"

## System Requirements 

JDK 8, Ant 1.9, Bourne shell 

## Installation

• Open a terminal and change to the grove directory 

• Enter 'ant' at the command prompt (without quotes) to compile the program. 

• Type 'chmod +x find_store' (in case find_store script execute permission unset by os security)

• find_store is a bourne shell wrapper for the java command required to run the executable

## Assumptions

• The 'java' and 'ant' commands must be in the user's execution path.

• I am assuming that the free geolocation API I selected (https://api.positionstack.com) returns
  the correct result. In fact, it returns a JSON array of potential matches. My code assumes that
  the first entry in that array is the right answer, although in testing, I definitely noticed that
  it makes mistakes, but it worked for most of the addresses I entered. Also, if there is a routing
  or connection failure, the timeout is too long (~20 seconds). 

