# BrainyArcade: Games Catalog (50 Logic Challenges)

**Universal Requirement**: Every game MUST feature dynamic level generation or a randomized start state to ensure infinite replayability. No gameplay session should be identical to a previous one.

## Implementation Standard for Replayability:
1. **Procedural Generators**: For grid-based logic (Sudoku, Nonograms).
2. **Shuffle Algorithms**: For arrangement games (15-Puzzle, Mahjong).
3. **Large Datasets**: Min. 10,000+ unique entries for static-logic puzzles (Wordle, Chess).

Games are ranked by general popularity and will be implemented in this order.

## 1. [x] Sudoku
- **Category**: Logic / Math
- **Benefit**: Pattern Recognition, Logical Deduction
- **Description**: 9x9 grid where rows, columns, and 3x3 subgrids must contain digits 1-9.
- **Plan**: Backtracking generator, note mode, mistake highlighting.
- **Guide**: Place 1-9 in every row, column, and 3x3 square without repetition.

## 2. [x] 2048
- **Category**: Math / Strategy
- **Benefit**: Mental Math, Spatial Awareness
- **Description**: Slide numbered tiles to merge identical values. Reach 2048.
- **Plan**: Grid sliding logic, merge mechanics, high score.
- **Guide**: Swipe or use D-pad to move all tiles. Same numbers merge when they touch.

## 3. [x] Minesweeper
- **Category**: Logic
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Clear a grid without hitting mines using proximity clues.
- **Plan**: Flood-fill clearing, long-press flagging.
- **Guide**: Numbers show how many mines are adjacent. Flag mines and clear safe squares.

## 4. [x] Wordle (Logic Variant)
- **Category**: Words
- **Benefit**: Vocabulary, Logical Deduction
- **Description**: Deduce a secret word in 6 tries with color feedback.
- **Plan**: Dictionary integration, daily puzzles.
- **Guide**: Guess the word. Green = correct spot, Yellow = wrong spot.

## 5. [x] Chess Puzzles (Tactics)
- **Category**: Strategy
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Solve specific scenarios (e.g., Mate in 1).
- **Plan**: FEN string parser, basic move validation.
- **Guide**: Identify the winning move to solve the puzzle.

## 6. [x] Tower of Hanoi
- **Category**: Spatial
- **Benefit**: Logical Deduction, Spatial Awareness
- **Description**: Move disks between 3 pegs; larger disks cannot go on smaller ones.
- **Plan**: Stack logic, recursion hint system.
- **Guide**: Move all disks to the third peg. Larger disks cannot be placed on smaller ones.

## 7. [x] Mastermind
- **Category**: Logic
- **Benefit**: Logical Deduction
- **Description**: Crack a 4-color code with black/white peg feedback.
- **Plan**: Clue calculation, interactive board.
- **Guide**: Guess the colors. Black = right spot, White = right color.

## 8. [x] Reversi (Othello)
- **Category**: Strategy
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Flip opponent disks by trapping them between your own.
- **Plan**: Board flip logic, Minimax AI.
- **Guide**: Trap opponent disks between your own to flip them.

## 9. [x] Nonograms (Picross)
- **Category**: Logic
- **Benefit**: Pattern Recognition
- **Description**: Reveal a picture by filling cells based on side clues.
- **Plan**: Clue generation, "X" marking.

## 10. [x] Sliding Puzzle (15-Puzzle)
- **Description**: Order numbered tiles by sliding them into a blank space.
- **Plan**: Solvability check, slide animations.

## 11. [x] Sokoban
- **Description**: Push crates to target locations in a warehouse.
- **Plan**: Level loader, undo/redo system.

## 12. [x] Simon Says
- **Category**: Memory
- **Benefit**: Train Memory
- **Description**: Recall and repeat an increasing sequence of colors/sounds.
- **Plan**: State machine for playback/input.

## 13. [x] Flow Free
- **Description**: Connect matching colors with pipes to cover the grid.
- **Plan**: Pathfinding validation.

## 14. [x] Lights Out
- **Category**: Logic
- **Benefit**: Pattern Recognition
- **Description**: Toggle a light and its neighbors to turn all lights off.
- **Plan**: XOR logic, random solvable setup.
- **Guide**: Toggle a light to flip it and its neighbors. Goal: Turn all off.

## 15. [x] Peg Solitaire
- **Description**: Jump pegs over neighbors to remove them. Goal: Leave one peg.
- **Plan**: Jump validation, English/French layouts.

## 16. [x] Battleship (Solo Logic)
- **Description**: Locate hidden ships using row/column count clues.
- **Plan**: Deduction-based puzzle generation.

## 17. [x] N-in-a-Row (Gomoku)
- **Description**: Place stones to get 5 in a row.
- **Plan**: Win-line detection, heuristic AI.

## 18. [x] Kakuro
- **Description**: Cross-sum puzzle. Fill cells so sums match clues.
- **Plan**: Sum validation, intersection checking.

## 19. [x] Bridges (Hashiwokakero)
- **Description**: Connect islands with bridges based on number clues.
- **Plan**: Planarity check, intersection logic.

## 20. [x] Hitori
- **Description**: Shade cells so numbers don't repeat in rows/columns.
- **Plan**: Adjacency and connectivity validation.

## 21. [x] Slitherlink (Loop the Loop)
- **Description**: Connect dots to form a single loop based on number clues.
- **Plan**: Loop validation, parity logic.

## 22. [x] Mahjong Solitaire (Logic Focused)
- **Description**: Match and remove identical free tiles.
- **Plan**: 3D layering logic, solvable deal generator.

## 23. [x] Tangram
- **Description**: Arrange 7 geometric pieces to fit a silhouette.
- **Plan**: Polygon collision/overlap detection.

## 24. [x] KenKen (Mathdoku)
- **Description**: Fill grid with 1-N using math operations in cages.
- **Plan**: Cage validation, unique value checking.

## 25. [x] Nurikabe
- **Description**: Form "islands" of unshaded cells based on numbers.
- **Plan**: BFS for island size and water connectivity.

## 26. [x] Fillomino
- **Description**: Divide grid into polyominoes of the specified sizes.
- **Plan**: Region-based validation.

## 27. [x] Futoshiki
- **Description**: Sudoku-like grid with greater-than/less-than constraints.
- **Plan**: Inequality validation.

## 28. [x] Shikaku
- **Description**: Divide grid into rectangles with specified areas.
- **Plan**: Area and overlap validation.

## 29. [x] Akari (Light Up)
- **Description**: Place lightbulbs to illuminate the grid without them seeing each other.
- **Plan**: Line-of-sight lighting logic.

## 30. [x] Masyu
- **Description**: Draw a loop through circles with specific turning rules.
- **Plan**: Loop segment tracking.

## 31. [x] Tents and Trees
- **Description**: Place a tent next to every tree based on count clues.
- **Plan**: Bipartite matching validation.

## 32. [x] Heyawake
- **Category**: Logic
- **Benefit**: Logical Deduction
- **Description**: Shade cells in rooms following adjacency and line-of-sight rules.
- **Plan**: Room constraint checking.
- **Guide**: Shade cells so no two are adjacent. unshaded lines cannot span more than two rooms. Each room clue must be met.

## 33. [x] Kuromasu
- **Category**: Logic
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Shade cells so numbers represent visible white cells.
- **Plan**: Visibility calculation.

## 34. [x] Lits
- **Category**: Logic
- **Benefit**: Pattern Recognition, Spatial Awareness
- **Description**: Place one tetromino in each region to form a connected unshaded area.
- **Plan**: Tetromino validation.
- **Guide**: Each region must contain one tetromino (L, I, T, S). All tetrominos must be connected. No 2x2 tetromino blocks.

## 35. [x] Yajilin
- **Category**: Logic
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Shade cells and draw a loop that avoids them.
- **Plan**: Arrow clue validation.
- **Guide**: Shade cells based on arrows. Shaded cells don't touch. Draw a single loop through all unshaded non-clue cells.

## 36. [x] Norinori
- **Category**: Logic
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Shade pairs of adjacent cells (dominoes) in each region.
- **Plan**: Pair validation.
- **Guide**: Each region must contain exactly two shaded cells. Shaded cells must form 2x1 or 1x2 dominoes.

## 37. [x] Shakashaka
- **Category**: Logic
- **Benefit**: Pattern Recognition, Spatial Awareness
- **Description**: Place triangles to form white rectangles/squares.
- **Plan**: Geometry slope validation.
- **Guide**: Place triangles in white cells. The remaining white space must form rectangles or squares.

## 38. [x] Gokigen Naname
- **Category**: Logic
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Draw diagonal lines so numbers match the count of intersecting lines.
- **Plan**: Node intersection count.
- **Guide**: Draw a diagonal in every cell. The node numbers show how many diagonals meet there. No closed loops.

## 39. [x] Marupeke
- **Category**: Logic
- **Benefit**: Pattern Recognition, Logical Deduction
- **Description**: Fill empty cells with X or O so no 3 are identical in a row.
- **Plan**: Sequence validation.
- **Guide**: Fill all empty cells with X or O. No more than two identical symbols can be consecutive in any direction.

## 40. [x] Numberlink
- **Category**: Logic
- **Benefit**: Pattern Recognition
- **Description**: Connect matching numbers with paths.
- **Plan**: Path collision check.
- **Guide**: Connect matching numbers with non-intersecting lines.

## 41. [x] Skyscraper
- **Category**: Logic
- **Benefit**: Logical Deduction, Spatial Awareness
- **Description**: Place buildings of heights 1-N based on visibility clues.
- **Plan**: Visibility (peak) counting.
- **Guide**: Fill grid with 1-N. Each row/col must have unique numbers. Clues show how many buildings are visible from that edge.

## 42. [x] Star Battle
- **Category**: Logic
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Place stars in regions so no two stars touch.
- **Plan**: 2D distance validation.
- **Guide**: Place two stars in each row, column, and region. Stars cannot touch even diagonally.

## 43. [x] Thermometers
- **Category**: Logic
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Fill thermometers based on row/column counts.
- **Plan**: Directional filling logic.
- **Guide**: Fill thermometers starting from the bulb end. Row/col counts show how many cells are filled.

## 44. [x] Tapa
- **Category**: Logic
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Shade cells to form a connected wall based on clues.
- **Plan**: Wall connectivity and "no 2x2" check.
- **Guide**: Shade cells to form a single wall. No 2x2 blocks. Clues show lengths of consecutive shaded blocks around that cell.

## 45. [x] Cave (Bag)
- **Category**: Logic
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Draw a loop to enclose all numbered cells.
- **Plan**: Visibility-based cave walls.
- **Guide**: Enclose all clues in a single loop. Numbers show visible unshaded cells in straight lines.

## 46. [x] Snake (Logic Variant)
- **Category**: Logic
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Draw a snake of a certain length avoiding itself.
- **Plan**: Head/Tail tracking.
- **Guide**: Draw a single path from head to tail. It cannot touch itself (even diagonally). Row/col counts must match.

## 47. [x] Ripple Effect
- **Category**: Logic
- **Benefit**: Logical Deduction
- **Description**: Place numbers in rooms where distance matches the number value.
- **Plan**: Spacing validation.
- **Guide**: Fill rooms with numbers 1-N. Identical numbers N in the same row/col must be separated by at least N other cells.

## 48. [x] ABC View
- **Category**: Logic
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Place A, B, C in rows/cols based on first-seen clues.
- **Plan**: Latin square + visibility logic.
- **Guide**: Each row/col contains each symbol once. Edge clues show which symbol is seen first from that direction.

## 49. [x] Domino Logic
- **Category**: Logic
- **Benefit**: Logical Deduction, Pattern Recognition
- **Description**: Reconstruct a set of dominoes on a grid.
- **Plan**: Pair matching.
- **Guide**: Divide the grid into 2x1 dominoes. Every domino in the set must be used once.

## 50. [x] Magic Square
- **Category**: Math
- **Benefit**: Mental Math, Logical Deduction
- **Description**: Arrange numbers so rows, columns, and diagonals sum to the same value.
- **Plan**: Magic constant validation.
- **Guide**: Place distinct numbers 1-N^2 so every row, column, and diagonal has the same sum.
