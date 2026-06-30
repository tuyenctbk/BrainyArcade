package com.tdpham.brainyarcade.hub

import com.tdpham.brainyarcade.R
import com.tdpham.brainyarcade.games.sudoku.SudokuActivity
import com.tdpham.brainyarcade.games.game2048.Game2048Activity
import com.tdpham.brainyarcade.games.minesweeper.MinesweeperActivity
import com.tdpham.brainyarcade.games.wordle.WordleActivity
import com.tdpham.brainyarcade.games.chess.ChessActivity
import com.tdpham.brainyarcade.games.hanoi.HanoiActivity
import com.tdpham.brainyarcade.games.mastermind.MastermindActivity
import com.tdpham.brainyarcade.games.reversi.ReversiActivity
import com.tdpham.brainyarcade.games.nonogram.NonogramActivity
import com.tdpham.brainyarcade.games.sliding.SlidingPuzzleActivity
import com.tdpham.brainyarcade.games.sokoban.SokobanActivity
import com.tdpham.brainyarcade.games.simon.SimonSaysActivity
import com.tdpham.brainyarcade.games.flow.FlowActivity
import com.tdpham.brainyarcade.games.battleship.BattleshipActivity
import com.tdpham.brainyarcade.games.gomoku.GomokuActivity
import com.tdpham.brainyarcade.games.kakuro.KakuroActivity
import com.tdpham.brainyarcade.games.bridges.BridgesActivity
import com.tdpham.brainyarcade.games.slitherlink.SlitherlinkActivity
import com.tdpham.brainyarcade.games.mahjong.MahjongActivity
import com.tdpham.brainyarcade.games.tangram.TangramActivity
import com.tdpham.brainyarcade.games.kenken.KenKenActivity
import com.tdpham.brainyarcade.games.nurikabe.NurikabeActivity
import com.tdpham.brainyarcade.games.akari.AkariActivity
import com.tdpham.brainyarcade.games.skyscraper.SkyscraperActivity
import com.tdpham.brainyarcade.games.starbattle.StarBattleActivity

object GameRegistry {
    val ALL_GAMES = listOf(
        // Tier 1: Blockbusters
        GameInfo("sudoku", R.string.game_sudoku_title, R.string.game_sudoku_desc, R.drawable.ic_game_sudoku, R.drawable.pattern_brainy, SudokuActivity::class.java, GameCategory.LOGIC, listOf(GameBenefit.LOGICAL_DEDUCTION, GameBenefit.PATTERN_RECOGNITION), R.string.game_sudoku_instr, ScoreType.TIME, ProgressionType.LEVELS),
        GameInfo("game_2048", R.string.game_2048_title, R.string.game_2048_desc, R.drawable.ic_game_2048, R.drawable.pattern_brainy, Game2048Activity::class.java, GameCategory.MATH, listOf(GameBenefit.MENTAL_MATH, GameBenefit.SPATIAL_AWARENESS), R.string.game_2048_instr, ScoreType.POINTS, ProgressionType.INFINITE),
        GameInfo("wordle", R.string.game_wordle_title, R.string.game_wordle_desc, R.drawable.ic_game_wordle, R.drawable.pattern_brainy, WordleActivity::class.java, GameCategory.WORDS, listOf(GameBenefit.VOCABULARY, GameBenefit.LOGICAL_DEDUCTION), R.string.game_wordle_instr, ScoreType.MOVES, ProgressionType.INFINITE),
        GameInfo("minesweeper", R.string.game_minesweeper_title, R.string.game_minesweeper_desc, R.drawable.ic_game_minesweeper, R.drawable.pattern_brainy, MinesweeperActivity::class.java, GameCategory.LOGIC, listOf(GameBenefit.LOGICAL_DEDUCTION, GameBenefit.PATTERN_RECOGNITION), R.string.game_minesweeper_instr, ScoreType.TIME, ProgressionType.LEVELS),
        GameInfo("flow_free", R.string.game_flow_title, R.string.game_flow_desc, R.drawable.ic_game_flow, R.drawable.pattern_brainy, FlowActivity::class.java, GameCategory.LOGIC, listOf(GameBenefit.PATTERN_RECOGNITION), R.string.game_flow_instr, ScoreType.TIME, ProgressionType.LEVELS),
        GameInfo("nonograms", R.string.game_nonogram_title, R.string.game_nonogram_desc, R.drawable.ic_game_nonogram, R.drawable.pattern_brainy, NonogramActivity::class.java, GameCategory.LOGIC, listOf(GameBenefit.PATTERN_RECOGNITION), R.string.game_nonogram_instr, ScoreType.TIME, ProgressionType.LEVELS),
        GameInfo("mahjong", R.string.game_mahjong_title, R.string.game_mahjong_desc, android.R.drawable.ic_menu_sort_alphabetically, R.drawable.pattern_brainy, MahjongActivity::class.java, GameCategory.LOGIC, listOf(GameBenefit.PATTERN_RECOGNITION, GameBenefit.SPATIAL_AWARENESS), R.string.game_mahjong_instr, ScoreType.POINTS, ProgressionType.LEVELS),
        GameInfo("sokoban", R.string.game_sokoban_title, R.string.game_sokoban_desc, android.R.drawable.ic_menu_directions, R.drawable.pattern_brainy, SokobanActivity::class.java, GameCategory.SPATIAL, listOf(GameBenefit.LOGICAL_DEDUCTION), R.string.game_sokoban_instr, ScoreType.MOVES, ProgressionType.LEVELS),
        GameInfo("chess", R.string.game_chess_title, R.string.game_chess_desc, R.drawable.ic_game_chess, R.drawable.pattern_brainy, ChessActivity::class.java, GameCategory.STRATEGY, listOf(GameBenefit.LOGICAL_DEDUCTION, GameBenefit.PATTERN_RECOGNITION), R.string.game_chess_instr, ScoreType.TIME, ProgressionType.LEVELS),
        GameInfo("simon_says", R.string.game_simon_title, R.string.game_simon_desc, android.R.drawable.ic_lock_silent_mode, R.drawable.pattern_brainy, SimonSaysActivity::class.java, GameCategory.MEMORY, listOf(GameBenefit.TRAIN_MEMORY), R.string.game_simon_instr, ScoreType.POINTS, ProgressionType.INFINITE),

        // Tier 2: Solid Classics
        GameInfo("battleship", R.string.game_battleship_title, R.string.game_battleship_desc, android.R.drawable.ic_menu_compass, R.drawable.pattern_brainy, BattleshipActivity::class.java, GameCategory.LOGIC, listOf(GameBenefit.LOGICAL_DEDUCTION), R.string.game_battleship_instr, ScoreType.TIME, ProgressionType.LEVELS),
        GameInfo("bridges", R.string.game_bridges_title, R.string.game_bridges_desc, android.R.drawable.ic_menu_share, R.drawable.pattern_brainy, BridgesActivity::class.java, GameCategory.LOGIC, listOf(GameBenefit.PATTERN_RECOGNITION), R.string.game_bridges_instr, ScoreType.TIME, ProgressionType.LEVELS),
        GameInfo("slitherlink", R.string.game_slitherlink_title, R.string.game_slitherlink_desc, android.R.drawable.ic_menu_rotate, R.drawable.pattern_brainy, SlitherlinkActivity::class.java, GameCategory.LOGIC, listOf(GameBenefit.LOGICAL_DEDUCTION, GameBenefit.PATTERN_RECOGNITION), R.string.game_slitherlink_instr, ScoreType.TIME, ProgressionType.LEVELS),
        GameInfo("gomoku", R.string.game_gomoku_title, R.string.game_gomoku_desc, android.R.drawable.ic_menu_edit, R.drawable.pattern_brainy, GomokuActivity::class.java, GameCategory.STRATEGY, listOf(GameBenefit.PATTERN_RECOGNITION), R.string.game_gomoku_instr, ScoreType.POINTS, ProgressionType.INFINITE),
        GameInfo("mastermind", R.string.game_mastermind_title, R.string.game_mastermind_desc, android.R.drawable.ic_menu_help, R.drawable.pattern_brainy, MastermindActivity::class.java, GameCategory.LOGIC, listOf(GameBenefit.LOGICAL_DEDUCTION), R.string.game_mastermind_instr, ScoreType.MOVES, ProgressionType.LEVELS),
        GameInfo("hanoi", R.string.game_hanoi_title, R.string.game_hanoi_desc, R.drawable.ic_game_hanoi, R.drawable.pattern_brainy, HanoiActivity::class.java, GameCategory.SPATIAL, listOf(GameBenefit.LOGICAL_DEDUCTION, GameBenefit.SPATIAL_AWARENESS), R.string.game_hanoi_instr, ScoreType.MOVES, ProgressionType.LEVELS),
        GameInfo("reversi", R.string.game_reversi_title, R.string.game_reversi_desc, android.R.drawable.ic_menu_manage, R.drawable.pattern_brainy, ReversiActivity::class.java, GameCategory.STRATEGY, listOf(GameBenefit.LOGICAL_DEDUCTION, GameBenefit.PATTERN_RECOGNITION), R.string.game_reversi_instr, ScoreType.POINTS, ProgressionType.INFINITE),
        GameInfo("sliding_puzzle", R.string.game_sliding_title, R.string.game_sliding_desc, android.R.drawable.ic_menu_sort_by_size, R.drawable.pattern_brainy, SlidingPuzzleActivity::class.java, GameCategory.SPATIAL, listOf(GameBenefit.SPATIAL_AWARENESS), R.string.game_sliding_instr, ScoreType.MOVES, ProgressionType.LEVELS),
        GameInfo("kakuro", R.string.game_kakuro_title, R.string.game_kakuro_desc, android.R.drawable.ic_menu_add, R.drawable.pattern_brainy, KakuroActivity::class.java, GameCategory.MATH, listOf(GameBenefit.MENTAL_MATH, GameBenefit.LOGICAL_DEDUCTION), R.string.game_kakuro_instr, ScoreType.TIME, ProgressionType.LEVELS),
        GameInfo("kenken", R.string.game_kenken_title, R.string.game_kenken_desc, android.R.drawable.ic_menu_send, R.drawable.pattern_brainy, KenKenActivity::class.java, GameCategory.MATH, listOf(GameBenefit.MENTAL_MATH, GameBenefit.LOGICAL_DEDUCTION), R.string.game_kenken_instr, ScoreType.TIME, ProgressionType.LEVELS),
        GameInfo("tangram", R.string.game_tangram_title, R.string.game_tangram_desc, android.R.drawable.ic_menu_crop, R.drawable.pattern_brainy, TangramActivity::class.java, GameCategory.SPATIAL, listOf(GameBenefit.SPATIAL_AWARENESS, GameBenefit.LOGICAL_DEDUCTION), R.string.game_tangram_instr, ScoreType.TIME, ProgressionType.LEVELS),
        GameInfo("starbattle", R.string.game_starbattle_title, R.string.game_starbattle_desc, android.R.drawable.ic_menu_view, R.drawable.pattern_brainy, StarBattleActivity::class.java, GameCategory.LOGIC, listOf(GameBenefit.LOGICAL_DEDUCTION, GameBenefit.PATTERN_RECOGNITION), R.string.game_starbattle_instr, ScoreType.TIME, ProgressionType.LEVELS),
        GameInfo("skyscraper", R.string.game_skyscraper_title, R.string.game_skyscraper_desc, android.R.drawable.ic_menu_sort_by_size, R.drawable.pattern_brainy, SkyscraperActivity::class.java, GameCategory.LOGIC, listOf(GameBenefit.LOGICAL_DEDUCTION, GameBenefit.SPATIAL_AWARENESS), R.string.game_skyscraper_instr, ScoreType.TIME, ProgressionType.LEVELS),
        
        // Tier 3: Selected Polish Gems
        GameInfo("akari", R.string.game_akari_title, R.string.game_akari_desc, android.R.drawable.ic_menu_compass, R.drawable.pattern_brainy, AkariActivity::class.java, GameCategory.LOGIC, listOf(GameBenefit.PATTERN_RECOGNITION, GameBenefit.LOGICAL_DEDUCTION), R.string.game_akari_instr, ScoreType.TIME, ProgressionType.LEVELS),
        GameInfo("nurikabe", R.string.game_nurikabe_title, R.string.game_nurikabe_desc, android.R.drawable.ic_menu_directions, R.drawable.pattern_brainy, NurikabeActivity::class.java, GameCategory.LOGIC, listOf(GameBenefit.LOGICAL_DEDUCTION, GameBenefit.PATTERN_RECOGNITION), R.string.game_nurikabe_instr, ScoreType.TIME, ProgressionType.LEVELS)
    )
}
