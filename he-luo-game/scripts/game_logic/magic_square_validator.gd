extends RefCounted
class_name MagicSquareValidator

const TARGET_SUM := 15
const GRID_SIZE := 3

static func is_valid_square(grid: Array) -> bool:
	if grid.size() != GRID_SIZE:
		return false
	for row in grid:
		if row.size() != GRID_SIZE:
			return false
	
	if not _all_numbers_present(grid):
		return false
	
	if not _check_rows(grid):
		return false
	
	if not _check_columns(grid):
		return false
	
	if not _check_diagonals(grid):
		return false
	
	return true

static func _all_numbers_present(grid: Array) -> bool:
	var numbers: Array[int] = []
	for row in grid:
		for cell in row:
			if cell == 0 or cell == null:
				return false
			numbers.append(cell)
	
	numbers.sort()
	for i in range(1, 10):
		if numbers[i - 1] != i:
			return false
	return true

static func _check_rows(grid: Array) -> bool:
	for row in grid:
		var row_sum := 0
		for cell in row:
			if cell == 0 or cell == null:
				return false
			row_sum += cell
		if row_sum != TARGET_SUM:
			return false
	return true

static func _check_columns(grid: Array) -> bool:
	for col in range(GRID_SIZE):
		var col_sum := 0
		for row in range(GRID_SIZE):
			var cell = grid[row][col]
			if cell == 0 or cell == null:
				return false
			col_sum += cell
		if col_sum != TARGET_SUM:
			return false
	return true

static func _check_diagonals(grid: Array) -> bool:
	var diag1_sum := 0
	var diag2_sum := 0
	
	for i in range(GRID_SIZE):
		var cell1 = grid[i][i]
		var cell2 = grid[i][GRID_SIZE - 1 - i]
		
		if cell1 == 0 or cell1 == null or cell2 == 0 or cell2 == null:
			return false
		
		diag1_sum += cell1
		diag2_sum += cell2
	
	return diag1_sum == TARGET_SUM and diag2_sum == TARGET_SUM

static func get_row_sums(grid: Array) -> Array[int]:
	var sums: Array[int] = []
	for row in grid:
		var row_sum := 0
		for cell in row:
			if cell != 0 and cell != null:
				row_sum += cell
		sums.append(row_sum)
	return sums

static func get_column_sums(grid: Array) -> Array[int]:
	var sums: Array[int] = []
	for col in range(GRID_SIZE):
		var col_sum := 0
		for row in range(GRID_SIZE):
			var cell = grid[row][col]
			if cell != 0 and cell != null:
				col_sum += cell
		sums.append(col_sum)
	return sums

static func get_diagonal_sums(grid: Array) -> Dictionary:
	var diag1_sum := 0
	var diag2_sum := 0
	
	for i in range(GRID_SIZE):
		var cell1 = grid[i][i]
		var cell2 = grid[i][GRID_SIZE - 1 - i]
		
		if cell1 != 0 and cell1 != null:
			diag1_sum += cell1
		if cell2 != 0 and cell2 != null:
			diag2_sum += cell2
	
	return {
		"main_diag": diag1_sum,
		"anti_diag": diag2_sum
	}

static func is_complete(grid: Array) -> bool:
	for row in grid:
		for cell in row:
			if cell == 0 or cell == null:
				return false
	return true

static func get_correct_answer() -> Array:
	return [
		[4, 9, 2],
		[3, 5, 7],
		[8, 1, 6]
	]

static func get_hint_for_position(grid: Array, row: int, col: int) -> int:
	var answer = get_correct_answer()
	return answer[row][col]
