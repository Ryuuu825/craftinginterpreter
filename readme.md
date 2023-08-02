# Crafting Interpreters

This is the repository for my work through the book [Crafting Interpreters](https://craftinginterpreters.com/).

## Extra Features

I added library import support to the Lox lang. It just grabs the java class in runtime and adds it to the global environment. It's not very robust, but it works just fine. I also added a few extra functions to the standard library.

example:

```rust
use std::math::max;

print(max(1,2))
```
