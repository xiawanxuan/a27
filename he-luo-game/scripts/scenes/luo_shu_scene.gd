extends Control

const GRID_SIZE := 3
const TARGET_SUM := 15

@onready var grid_container: GridContainer = $MainPanel/VBox/GridContainer
@onready var numbers_panel: PanelContainer = $MainPanel/VBox/NumbersPanel
@onready var numbers_hbox: HBoxContainer = $MainPanel/VBox/NumbersPanel/NumbersHBox
@onready var row_sums_label: Label = $MainPanel/VBox/SumsPanel/RowSumsLabel
@onready var col_sums_label: Label = $MainPanel/VBox/SumsPanel/ColSumsLabel
@onready var diag_sums_label: Label = $MainPanel/VBox/SumsPanel/DiagSumsLabel
@onready var hint_button: Button = $MainPanel/VBox/ControlPanel/HintButton
@onready var reset_button: Button = $MainPanel/VBox/ControlPanel/ResetButton
@onready var back_button: Button = $MainPanel/VBox/ControlPanel/BackButton
@onready var title_label: Label = $MainPanel/VBox/TitleLabel

var grid_slots: Array[GridSlot] = []
var number_tiles: Array[NumberTile] = []
var grid: Array = []
var is_completed: bool = false
var hint_used: int = 0

var number_tile_scene: PackedScene
var grid_slot_scene: PackedScene

func _ready() -> void:
	number_tile_scene = preload("res://scenes/components/number_tile.tscn")
	_init_grid()
	_init_number_tiles()
	_connect_signals()
	_load_game_state()
	_update_sums_display()

func _init_grid() -> void:
	grid = []
	for r in range(GRID_SIZE):
		var row: Array[int] = []
		for c in range(GRID_SIZE):
			row.append(0)
			
			var slot = GridSlot.new()
			slot.set_position_grid(r, c)
			slot.custom_minimum_size = Vector2(80, 80)
			slot.size_flags_horizontal = Control.SIZE_SHRINK_CENTER
			slot.size_flags_vertical = Control.SIZE_SHRINK_CENTER
			
			var bg := ColorRect.new()
			bg.color = Color(0.15, 0.2, 0.35, 0.8)
			bg.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
			slot.add_child(bg)
			
			grid_container.add_child(slot)
			grid_slots.append(slot)
			
			slot.item_dropped.connect(_on_slot_item_dropped)
			slot.item_removed.connect(_on_slot_item_removed)
		
		grid.append(row)

func _init_number_tiles() -> void:
	var numbers: Array[int] = [1, 2, 3, 4, 5, 6, 7, 8, 9]
	numbers.shuffle()
	
	for n in numbers:
		var tile = number_tile_scene.instantiate()
		tile.set_number(n)
		tile.item_color = Color(0.95, 0.85, 0.9, 1)
		tile.custom_minimum_size = Vector2(50, 50)
		numbers_hbox.add_child(tile)
		number_tiles.append(tile)
		tile.drag_ended.connect(_on_tile_drag_ended)

func _connect_signals() -> void:
	hint_button.pressed.connect(_on_hint_pressed)
	reset_button.pressed.connect(_on_reset_pressed)
	back_button.pressed.connect(_on_back_pressed)

func _on_slot_item_dropped(item: DraggableItem, slot: DropSlot) -> void:
	if item is NumberTile and slot is GridSlot:
		grid[slot.row][slot.col] = item.number_value
		_check_completion()
		_save_game_state()
		_update_sums_display()

func _on_slot_item_removed(item: DraggableItem, slot: DropSlot) -> void:
	if slot is GridSlot:
		grid[slot.row][slot.col] = 0
		_update_sums_display()

func _on_tile_drag_ended(item: DraggableItem, dropped: bool) -> void:
	_update_sums_display()

func _update_sums_display() -> void:
	var row_sums = MagicSquareValidator.get_row_sums(grid)
	var col_sums = MagicSquareValidator.get_column_sums(grid)
	var diag_sums = MagicSquareValidator.get_diagonal_sums(grid)
	
	var row_text := "行和: "
	for i in range(row_sums.size()):
		var sum_val = row_sums[i]
		var color_tag := "[color=white]"
		if sum_val == TARGET_SUM and _is_row_complete(i):
			color_tag = "[color=green]"
		elif sum_val > TARGET_SUM:
			color_tag = "[color=red]"
		row_text += "%s%d[/color] " % [color_tag, sum_val]
	row_sums_label.text = row_text
	
	var col_text := "列和: "
	for i in range(col_sums.size()):
		var sum_val = col_sums[i]
		var color_tag := "[color=white]"
		if sum_val == TARGET_SUM and _is_col_complete(i):
			color_tag = "[color=green]"
		elif sum_val > TARGET_SUM:
			color_tag = "[color=red]"
		col_text += "%s%d[/color] " % [color_tag, sum_val]
	col_sums_label.text = col_text
	
	var diag_main = diag_sums["main_diag"]
	var diag_anti = diag_sums["anti_diag"]
	
	var main_color := "[color=white]"
	if diag_main == TARGET_SUM and _is_main_diag_complete():
		main_color = "[color=green]"
	elif diag_main > TARGET_SUM:
		main_color = "[color=red]"
	
	var anti_color := "[color=white]"
	if diag_anti == TARGET_SUM and _is_anti_diag_complete():
		anti_color = "[color=green]"
	elif diag_anti > TARGET_SUM:
		anti_color = "[color=red]"
	
	diag_sums_label.text = "对角线: %s%d[/color] %s%d[/color]" % [main_color, diag_main, anti_color, diag_anti]

func _is_row_complete(row_idx: int) -> bool:
	for c in range(GRID_SIZE):
		if grid[row_idx][c] == 0:
			return false
	return true

func _is_col_complete(col_idx: int) -> bool:
	for r in range(GRID_SIZE):
		if grid[r][col_idx] == 0:
			return false
	return true

func _is_main_diag_complete() -> bool:
	for i in range(GRID_SIZE):
		if grid[i][i] == 0:
			return false
	return true

func _is_anti_diag_complete() -> bool:
	for i in range(GRID_SIZE):
		if grid[i][GRID_SIZE - 1 - i] == 0:
			return false
	return true

func _check_completion() -> void:
	if not MagicSquareValidator.is_complete(grid):
		return
	
	if MagicSquareValidator.is_valid_square(grid):
		_on_victory()
	else:
		_show_wrong_feedback()

func _on_victory() -> void:
	is_completed = true
	
	for slot in grid_slots:
		if slot.current_item is NumberTile:
			slot.current_item.set_correct_state(true)
	
	if GameManager:
		GameManager.complete_level("luo_shu")
	
	var ui_manager = _get_ui_manager()
	if ui_manager:
		ui_manager.show_victory("洛书九宫格完成！\n幻方验证成功！")
		if not ui_manager.victory_continue_pressed.is_connected(_on_victory_continue):
			ui_manager.victory_continue_pressed.connect(_on_victory_continue)
		if not ui_manager.victory_replay_pressed.is_connected(_on_victory_replay):
			ui_manager.victory_replay_pressed.connect(_on_victory_replay)
	
	_save_game_state()

func _on_victory_continue() -> void:
	if GameManager and GameManager.is_level_unlocked("he_tu"):
		GameManager.goto_he_tu()
	else:
		_on_back_pressed()

func _on_victory_replay() -> void:
	_on_reset_pressed()

func _show_wrong_feedback() -> void:
	for slot in grid_slots:
		slot.show_wrong_feedback()

func _on_hint_pressed() -> void:
	for r in range(GRID_SIZE):
		for c in range(GRID_SIZE):
			if grid[r][c] == 0:
				var correct_num = MagicSquareValidator.get_hint_for_position(grid, r, c)
				var slot_idx = r * GRID_SIZE + c
				var slot = grid_slots[slot_idx]
				slot.show_correct_feedback()
				
				hint_used += 1
				var ui_manager = _get_ui_manager()
				if ui_manager:
					ui_manager.show_hint("第%d行第%d列应该是: %d" % [r + 1, c + 1, correct_num])
				return

func _on_reset_pressed() -> void:
	for slot in grid_slots:
		if slot.current_item:
			var item = slot.remove_item()
			if item:
				item.reset_to_original()
	
	grid = []
	for r in range(GRID_SIZE):
		var row: Array[int] = []
		for c in range(GRID_SIZE):
			row.append(0)
		grid.append(row)
	
	is_completed = false
	_update_sums_display()

func _on_back_pressed() -> void:
	_save_game_state()
	if GameManager:
		GameManager.goto_main_menu()

func _save_game_state() -> void:
	if GameManager and GameManager.save_system:
		var numbers_on_grid: Array = []
		for r in range(GRID_SIZE):
			for c in range(GRID_SIZE):
				numbers_on_grid.append(grid[r][c])
		
		var remaining_numbers: Array[int] = []
		for tile in number_tiles:
			if tile.get_parent() == numbers_hbox:
				remaining_numbers.append(tile.number_value)
		
		var data := {
			"type": "luo_shu",
			"grid": grid,
			"remaining_numbers": remaining_numbers,
			"completed": is_completed,
			"hint_used": hint_used
		}
		GameManager.save_system.auto_save(data)

func _load_game_state() -> void:
	if not GameManager or not GameManager.save_system:
		return
	
	var data = GameManager.save_system.load_luo_shu_state()
	if data.is_empty() or data.get("completed", false):
		return
	
	var saved_grid = data.get("grid", [])
	if saved_grid.is_empty() or saved_grid.size() != GRID_SIZE:
		return
	
	var remaining_numbers: Array[int] = data.get("remaining_numbers", [])
	
	for tile in number_tiles:
		tile.queue_free()
	number_tiles.clear()
	
	for r in range(GRID_SIZE):
		for c in range(GRID_SIZE):
			var value = saved_grid[r][c]
			if value > 0:
				var tile = number_tile_scene.instantiate()
				tile.set_number(value)
				tile.item_color = Color(0.95, 0.85, 0.9, 1)
				tile.custom_minimum_size = Vector2(50, 50)
				tile.drag_ended.connect(_on_tile_drag_ended)
				
				var slot_idx = r * GRID_SIZE + c
				grid_slots[slot_idx].accept_item(tile)
				number_tiles.append(tile)
				grid[r][c] = value
	
	for n in remaining_numbers:
		var tile = number_tile_scene.instantiate()
		tile.set_number(n)
		tile.item_color = Color(0.95, 0.85, 0.9, 1)
		tile.custom_minimum_size = Vector2(50, 50)
		numbers_hbox.add_child(tile)
		number_tiles.append(tile)
		tile.drag_ended.connect(_on_tile_drag_ended)

func _get_ui_manager() -> UIManager:
	var ui_manager = get_tree().get_first_node_in_group("ui_manager")
	if ui_manager and ui_manager is UIManager:
		return ui_manager
	return null
