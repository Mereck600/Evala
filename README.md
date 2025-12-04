# Evala
Evala is a language built to be seamless.
Evala changes user experience through
terminal outputs and generated files.
Simply write code, and Evala does the rest.​

## Purpose: 
Evala exists to make coding easier. One of the
more tedious aspects of programming is creating test
cases and making sure your code is robust. Evala takes
care of that for you! It reads the program you wrote,
gives you a grade out of 100 on it, and creates tests
for you to run.​

## Test Programs: 
### Sample 1 (Program without test cases):
- Sample.eva
```
// One-line comment (counts toward comment density)

/* 
   block comment 
*/

var used = 11;               // used later, allowed magic (1)
var temp = 42;              // unused local + magic number (42)
var bleh = 21;

fun add(a, b, debug,twenty) {      // 'debug' is unused
  if (a < b) {              // if without else
    return a + b + 3.14;    // magic number 3.14
  }
   
  return a + b;
}

fun sub(a, b, debug) {      // 'debug' is unused
  if (a < b) {              // if without else
    return a - b - 3.14;    // magic number 3.14
  }
   
  return a - b;
}

print add(used, 2, 0,0);      // 2 not whitelisted

```
### Sample 1 Output:
Terminal Output:
```
Test cases generated to: CodeReview/EvalaTests_add.eva
Test cases generated to: CodeReview/EvalaTests_sub.eva
Grade written to: CodeReview/GradedCode.md 
Total Grade: 48.44/100.0

-------------------------
Code Execution output ...
13
```
Test File Output:
- EvalTests_add.ava
- Note: there is also a file generated for sub function
```
// =======================================================
//            Evala Generated Test Files
//     Note: These tests may not be comprehensive.
//   Usage: Fill in expectedOutput and run the file.
// ========================================================


TestCases("add", 467568, 467568, nil, nil, "expectedOutput");


TestCases("add", 467568, 0.0, nil, nil, "expectedOutput");


TestCases("add", 467568, -741407, nil, nil, "expectedOutput");


TestCases("add", 0.0, 467568, nil, nil, "expectedOutput");


TestCases("add", 0.0, 0.0, nil, nil, "expectedOutput");


TestCases("add", 0.0, -741407, nil, nil, "expectedOutput");


TestCases("add", -741407, 467568, nil, nil, "expectedOutput");


TestCases("add", -741407, 0.0, nil, nil, "expectedOutput");


TestCases("add", -741407, -741407, nil, nil, "expectedOutput");


runTests(t0, t1, t2, t3, t4, t5, t6, t7, t8, t9 );
```
GradedCode.md
```
# Evala static grading

// 0.0/20.0
If without else: 2

// 17.0/20.0
Magic numbers: 3
  3.14
  3.14
  2.0

// 6.0/20.0
Unused locals: 2
  temp
  bleh

// 11.0/20.0
Unused parameters: 3
  function add: debug
  function add: twenty
  function sub: debug

// 14.44/20.0
Comment density:
  total lines: 27
  code lines:  16
  comment lines: 12
  ratio: 44.4%
  verdict: Too many comments

 # Total Grade: 48.44/100.0
```

### Sample 2 (Program with test cases):
-SampleWithTests.eva
```
fun foo(a, b, debug) {
  if (a < b) {
    return a + b;
  }
  return a + b;
}

TestCases("foo",100.0, 0.0, nil, 100.0);
TestCases("foo", 343.0, -452.0, nil, 795.0);
TestCases("foo", 343.0, -452.0, nil, -109.0);

print runTests();
```
### Sample 2 Output:
Terminal Output:
```
Test cases generated to: CodeReview/EvalaTests_foo.eva
Grade written to: CodeReview/GradedCode.md 
Total Grade: 45/100.0

-------------------------
Code Execution output ...

[PASS] foo[100.0, 0.0, null] == 100.0
[FAIL] foo[343.0, -452.0, null] expected: 795.0, got: -109.0
[PASS] foo[343.0, -452.0, null] == -109.0
--- TEST SUMMARY ---
Passed 2 / 3
```
Test File Output:
```
// =======================================================
//            Evala Generated Test Files
//     Note: These tests may not be comprehensive.
//   Usage: Fill in expectedOutput and run the file.
// ========================================================


TestCases("foo", 690521, 690521, nil, "expectedOutput");


TestCases("foo", 690521, 0.0, nil, "expectedOutput");


TestCases("foo", 690521, -715089, nil, "expectedOutput");


TestCases("foo", 0.0, 690521, nil, "expectedOutput");


TestCases("foo", 0.0, 0.0, nil, "expectedOutput");


TestCases("foo", 0.0, -715089, nil, "expectedOutput");


TestCases("foo", -715089, 690521, nil, "expectedOutput");


TestCases("foo", -715089, 0.0, nil, "expectedOutput");


TestCases("foo", -715089, -715089, nil, "expectedOutput");


runTests(t0, t1, t2, t3, t4, t5, t6, t7, t8, t9 );
```
GradedCoded.md:
```
# Evala static grading

// 0.0/20.0
If without else: 1

// 12.0/20.0
Magic numbers: 8
  100.0
  100.0
  343.0
  452.0
  795.0
  343.0
  452.0
  109.0

// 20.0/20.0
Unused locals: 0

// 13.0/20.0
Unused parameters: 1
  function foo: debug

// 0/20.0
Comment density:
  total lines: 13
  code lines:  10
  comment lines: 0
  ratio: 0.0%
  verdict: Good

 # Total Grade: 45/100.0

```



  
## Next Steps
- Create a way to build tests for string-based functions.
- Allow users to have the option not to have the code graded and tests generated.
- Expand the criteria for what makes robust code, and modify the grading parameters.


## Attributions:
- Evala is based on the work done by Robert Nystrom and his book (https://craftinginterpreters.com/contents.html)[Crafting Interpreters]
- With special thanks to Dr. Nadeem Abdul Hamid for his guidance.
  


