extends Control

const GRID_SIZE := 3
const TARGET_SUM := 15

@onready var grid_container: GridContainer = $MainPanel/VBox/GridContainer
@onready var numbers_panel: PanelContainer = $MainPanel/VBox/NumbersPanel
@onready var numbers_hbox: HBoxContainer = $MainPanel/VBox/NumbersPanel/NumbersHBox
@onready var row_sums_label: Label = $MainPanel/VBox/SumsPanel/RowSumsLabel
@onready var col_sums_label: Label = $MainPanel/VBox/SumsPanel/ColSumsLabel
@onready var diag_sums_label: Label = $MainPanel/VBox/SumsPanel/DiagSumsLabel
@onready var undo_button: Button = $MainPanel/VBox/ControlPanel/UndoButton
@onready var redo_button: Button = $MainPanel/VBox/ControlPanel/RedoButton
@onready var hint_button: Button = $MainPanel/VBox/ControlPanel/HintButton
@onready var bagua_button: Button = $MainPanel/VBox/ControlPanel/BaguaButton
@onready var reset_button: Button = $MainPanel/VBox/ControlPanel/ResetButton
@onready var back_button: Button = $MainPanel/VBox/ControlPanel/BackButton
@onready var title_label: Label = $MainPanel/VBox/TitleLabel

var grid_slots: Array[GridSlot] = []
var number_tiles: Array[NumberTile] = []
var grid: Array = []
var is_completed: bool = false
var hint_used: int = 0
var history: HistoryManager = null
var _suppress_history: bool = false
var _pending_history: Dictionary = {}

var number_tile_scene: PackedScene
var grid_slot_scene: PackedScene

func _ready() -> void:
	number_tile_scene = preload("res://scenes/components/number_tile.tscn")
	_init_history()
	_init_grid()
	_init_number_tiles()
	_connect_signals()
	_load_game_state()
	_update_sums_display()
	_update_undo_buttons()

func _init_history() -> void:
	history = HistoryManager.new()
	add_child(history)
	history.history_changed.connect(_on_history_changed)

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
	undo_button.pressed.connect(_on_undo_pressed)
	redo_button.pressed.connect(_on_redo_pressed)
	hint_button.pressed.connect(_on_hint_pressed)
	bagua_button.pressed.connect(_on_bagua_pressed)
	reset_button.pressed.connect(_on_reset_pressed)
	back_button.pressed.connect(_on_back_pressed)

func _on_slot_item_dropped(item: DraggableItem, slot: DropSlot) -> void:
	if item is NumberTile and slot is GridSlot:
		var old_value = grid[slot.row][slot.col]
		grid[slot.row][slot.col] = item.number_value
		
		if not _suppress_history:
			if _pending_history.is_empty():
				_pending_history = {
					"type": "luo_shu_drag",
					"to_slot": {"row": slot.row, "col": slot.col, "value": item.number_value},
					"from_slot": null,
					"from_panel": true
				}
			else:
				_pending_history["to_slot"] = {"row": slot.row, "col": slot.col, "value": item.number_value}
		
		_check_completion()
		_save_game_state()
		_update_sums_display()

func _on_slot_item_removed(item: DraggableItem, slot: DropSlot) -> void:
	if slot is GridSlot:
		grid[slot.row][slot.col] = 0
		
		if not _suppress_history:
			_pending_history = {
				"type": "luo_shu_drag",
				"from_slot": {"row": slot.row, "col": slot.col, "value": item.number_value if item is NumberTile else 0},
				"to_slot": null,
				"from_panel": false
			}
		
		_update_sums_display()

func _on_tile_drag_ended(item: DraggableItem, dropped: bool) -> void:
	if dropped and not _pending_history.is_empty() and not _suppress_history:
		history.push_action(_pending_history)
		_update_undo_buttons()
	_pending_history = {}
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

func _on_history_changed(can_undo: bool, can_redo: bool) -> void:
	_update_undo_buttons()

func _update_undo_buttons() -> void:
	if undo_button and history:
		undo_button.disabled = not history.can_undo()
	if redo_button and history:
		redo_button.disabled = not history.can_redo()

func _on_undo_pressed() -> void:
	if not history or not history.can_undo():
		return
	
	var action = history.undo()
	if action.is_empty():
		return
	
	_apply_history_action(action, true)
	_save_game_state()
	_update_sums_display()

func _on_redo_pressed() -> void:
	if not history or not history.can_redo():
		return
	
	var action = history.redo()
	if action.is_empty():
		return
	
	_apply_history_action(action, false)
	_save_game_state()
	_update_sums_display()

func _apply_history_action(action: Dictionary, is_undo: bool) -> void:
	_suppress_history = true
	
	var from_slot: Dictionary = action.get("from_slot", null)
	var to_slot: Dictionary = action.get("to_slot", null)
	var from_panel: bool = action.get("from_panel", true)
	
	if is_undo:
		if to_slot:
			var t_row = to_slot.row
			var t_col = to_slot.col
			var slot_idx = t_row * GRID_SIZE + t_col
			var slot = grid_slots[slot_idx]
			if slot and slot.has_item():
				var item = slot.remove_item()
				if item and from_panel:
					item.reset_to_original()
				elif item and from_slot:
					var f_row = from_slot.row
					var f_col = from_slot.col
					var f_slot_idx = f_row * GRID_SIZE + f_col
					var f_slot = grid_slots[f_slot_idx]
					if f_slot:
						f_slot.accept_item(item)
						grid[f_row][f_col] = from_slot.value
				grid[t_row][t_col] = 0
		
		if from_slot and not to_slot:
			var f_row = from_slot.row
			var f_col = from_slot.col
			var f_slot_idx = f_row * GRID_SIZE + f_col
			var f_slot = grid_slots[f_slot_idx]
			if f_slot and not f_slot.has_item():
				for tile in number_tiles:
					if tile.number_value == from_slot.value and tile.get_parent() == numbers_hbox:
						f_slot.accept_item(tile)
						grid[f_row][f_col] = from_slot.value
						break
	else:
		if to_slot:
			var t_row = to_slot.row
			var t_col = to_slot.col
			var t_slot_idx = t_row * GRID_SIZE + t_col
			var t_slot = grid_slots[t_slot_idx]
			
			if from_slot:
				var f_row = from_slot.row
				var f_col = from_slot.col
				var f_slot_idx = f_row * GRID_SIZE + f_col
				var f_slot = grid_slots[f_slot_idx]
				if f_slot and f_slot.has_item():
					var item = f_slot.remove_item()
					if item and t_slot:
						t_slot.accept_item(item)
						grid[f_row][f_col] = 0
						grid[t_row][t_col] = to_slot.value
			elif from_panel:
				for tile in number_tiles:
					if tile.number_value == to_slot.value and tile.get_parent() == numbers_hbox:
						if t_slot:
							t_slot.accept_item(tile)
							grid[t_row][t_col] = to_slot.value
						break
	
	_suppress_history = false

func _on_bagua_pressed() -> void:
	if not MagicSquareValidator.is_valid_square(grid):
		var ui_manager = _get_ui_manager()
		if ui_manager:
			ui_manager.show_hint("请先完成洛书九宫格再推演八卦！")
		return
	
	_show_bagua_dialog()

func _show_bagua_dialog() -> void:
	var dialog := AcceptDialog.new()
	dialog.title = "卦象推演"
	dialog.ok_button_text = "关闭"
	dialog.custom_minimum_size = Vector2(480, 520)
	
	var vbox := VBoxContainer.new()
	vbox.add_theme_constant_override("separation", 10)
	vbox.offset_left = 10
	vbox.offset_top = 10
	vbox.offset_right = -10
	vbox.offset_bottom = -10
	
	var title_label := Label.new()
	title_label.text = "先天八卦与后天八卦"
	title_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title_label.add_theme_font_size_override("font_size", 20)
	vbox.add_child(title_label)
	
	var tab_container := TabContainer.new()
	tab_container.custom_minimum_size = Vector2(420, 400)
	vbox.add_child(tab_container)
	
	var prenatal_panel := _create_bagua_panel(true)
	tab_container.add_child(prenatal_panel)
	tab_container.set_tab_title(0, "先天八卦")
	
	var postnatal_panel := _create_bagua_panel(false)
	tab_container.add_child(postnatal_panel)
	tab_container.set_tab_title(1, "后天八卦")
	
	dialog.add_child(vbox)
	add_child(dialog)
	dialog.popup_centered()

func _create_bagua_panel(is_prenatal: bool) -> Control:
	var panel := Panel.new()
	panel.custom_minimum_size = Vector2(400, 380)
	
	var vbox := VBoxContainer.new()
	vbox.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	vbox.add_theme_constant_override("separation", 8)
	vbox.offset_left = 10
	vbox.offset_top = 10
	vbox.offset_right = -10
	vbox.offset_bottom = -10
	panel.add_child(vbox)
	
	var bagua_data: Array
	if is_prenatal:
		bagua_data = BaguaSystem.get_prenatal_bagua_from_luoshu(grid)
	else:
		bagua_data = BaguaSystem.get_postnatal_bagua_from_luoshu(grid)
	
	var desc_label := Label.new()
	desc_label.text = "点击卦象查看详细解释"
	desc_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	vbox.add_child(desc_label)
	
	var grid := GridContainer.new()
	grid.columns = 3
	grid.add_theme_constant_override("h_separation", 5)
	grid.add_theme_constant_override("v_separation", 5)
	vbox.add_child(grid)
	
	var display_grid = [
		[null, null, null],
		[null, null, null],
		[null, null, null]
	]
	
	for data in bagua_data:
		if data.has("row") and data.has("col"):
			display_grid[data.row][data.col] = data
	
	for r in range(3):
		for c in range(3):
			var cell_data = display_grid[r][c]
			var cell_panel := PanelContainer.new()
			cell_panel.custom_minimum_size = Vector2(100, 90)
			
			if cell_data:
				var trigram_name: String = cell_data.trigram
				var number: int = cell_data.number
				
				var stylebox := StyleBoxFlat.new()
				var element = BaguaSystem.get_trigram_element(trigram_name)
				if element == "金":
					stylebox.bg_color = Color(0.85, 0.85, 0.95, 0.8)
				elif element == "木":
					stylebox.bg_color = Color(0.3, 0.7, 0.4, 0.8)
				elif element == "水":
					stylebox.bg_color = Color(0.3, 0.5, 0.85, 0.8)
				elif element == "火":
					stylebox.bg_color = Color(0.9, 0.4, 0.3, 0.8)
				elif element == "土":
					stylebox.bg_color = Color(0.8, 0.65, 0.35, 0.8)
				stylebox.corner_radius_top_left = 6
				stylebox.corner_radius_top_right = 6
				stylebox.corner_radius_bottom_right = 6
				stylebox.corner_radius_bottom_left = 6
				cell_panel.add_theme_stylebox_override("panel", stylebox)
				
				var cell_vbox := VBoxContainer.new()
				cell_vbox.alignment = BoxContainer.ALIGNMENT_CENTER
				cell_vbox.add_theme_constant_override("separation", 2)
				
				var symbol_label := Label.new()
				symbol_label.text = BaguaSystem.get_trigram_symbol(trigram_name)
				symbol_label.add_theme_font_size_override("font_size", 28)
				symbol_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
				symbol_label.add_theme_color_override("font_color", Color.BLACK)
				cell_vbox.add_child(symbol_label)
				
				var name_label := Label.new()
				name_label.text = trigram_name
				name_label.add_theme_font_size_override("font_size", 14)
				name_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
				name_label.add_theme_color_override("font_color", Color.BLACK)
				cell_vbox.add_child(name_label)
				
				var num_label := Label.new()
				num_label.text = "数: %d" % number
				num_label.add_theme_font_size_override("font_size", 12)
				num_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
				num_label.add_theme_color_override("font_color", Color.BLACK)
				cell_vbox.add_child(num_label)
				
				cell_panel.add_child(cell_vbox)
				cell_panel.mouse_filter = Control.MOUSE_FILTER_STOP
				cell_panel.gui_input.connect(_on_trigram_gui_input.bind(trigram_name))
			else:
				var stylebox := StyleBoxFlat.new()
				stylebox.bg_color = Color(0.2, 0.2, 0.3, 0.5)
				stylebox.corner_radius_top_left = 6
				stylebox.corner_radius_top_right = 6
				stylebox.corner_radius_bottom_right = 6
				stylebox.corner_radius_bottom_left = 6
				cell_panel.add_theme_stylebox_override("panel", stylebox)
				
				var center_label := Label.new()
				center_label.text = "中\n5"
				center_label.add_theme_font_size_override("font_size", 18)
				center_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
				center_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
				center_label.add_theme_color_override("font_color", Color(0.7, 0.7, 0.7))
				cell_panel.add_child(center_label)
			
			grid.add_child(cell_panel)
	
	return panel

func _on_trigram_gui_input(event: InputEvent, trigram_name: String) -> void:
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		_show_trigram_explanation(trigram_name)

func _show_trigram_explanation(trigram_name: String) -> void:
	var explanation = BaguaSystem.get_trigram_explanation(trigram_name)
	if explanation.is_empty():
		return
	
	var dialog := AcceptDialog.new()
	dialog.title = "%s - 卦象详解" % BaguaSystem.get_trigram_symbol(trigram_name) + " " + trigram_name
	dialog.ok_button_text = "关闭"
	dialog.custom_minimum_size = Vector2(400, 350)
	
	var vbox := VBoxContainer.new()
	vbox.add_theme_constant_override("separation", 10)
	vbox.custom_minimum_size = Vector2(380, 300)
	
	var symbol_label := Label.new()
	symbol_label.text = BaguaSystem.get_trigram_symbol(trigram_name)
	symbol_label.add_theme_font_size_override("font_size", 64)
	symbol_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	vbox.add_child(symbol_label)
	
	var name_label := Label.new()
	name_label.text = explanation.get("name", "")
	name_label.add_theme_font_size_override("font_size", 24)
	name_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	vbox.add_child(name_label)
	
	var info_grid := GridContainer.new()
	info_grid.columns = 2
	info_grid.add_theme_constant_override("h_separation", 10)
	info_grid.add_theme_constant_override("v_separation", 5)
	
	var labels := [
		"五行:",
		"自然:",
		"方位:",
		"德性:"
	]
	var values := [
		explanation.get("element", ""),
		explanation.get("nature", ""),
		explanation.get("direction", ""),
		explanation.get("virtue", "")
	]
	
	for i in range(labels.size()):
		var label1 := Label.new()
		label1.text = labels[i]
		label1.modulate = Color(0.7, 0.7, 0.7)
		info_grid.add_child(label1)
		
		var label2 := Label.new()
		label2.text = values[i]
		info_grid.add_child(label2)
	
	vbox.add_child(info_grid)
	
	var meaning_label := Label.new()
	meaning_label.text = "象征: %s" % explanation.get("meaning", "")
	meaning_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	vbox.add_child(meaning_label)
	
	var desc_label := Label.new()
	desc_label.text = explanation.get("description", "")
	desc_label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	desc_label.custom_minimum_size = Vector2(360, 0)
	vbox.add_child(desc_label)
	
	dialog.add_child(vbox)
	add_child(dialog)
	dialog.popup_centered()

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
	if history:
		history.clear()
	_update_sums_display()
	_update_undo_buttons()

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
	if data.is_empty():
		return
	
	var saved_grid = data.get("grid", [])
	if saved_grid.is_empty() or saved_grid.size() != GRID_SIZE:
		return
	
	var remaining_numbers: Array[int] = data.get("remaining_numbers", [])
	var saved_completed: bool = data.get("completed", false)
	var saved_hint_used: int = data.get("hint_used", 0)
	
	for slot in grid_slots:
		slot.clear_slot()
	
	for tile in number_tiles:
		if is_instance_valid(tile):
			tile.queue_free()
	number_tiles.clear()
	
	grid = []
	for r in range(GRID_SIZE):
		var row: Array[int] = []
		for c in range(GRID_SIZE):
			row.append(0)
		grid.append(row)
	
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
	
	is_completed = saved_completed
	hint_used = saved_hint_used
	
	if is_completed:
		for slot in grid_slots:
			if slot.current_item is NumberTile:
				slot.current_item.set_correct_state(true)

func _get_ui_manager() -> UIManager:
	var ui_manager = get_tree().get_first_node_in_group("ui_manager")
	if ui_manager and ui_manager is UIManager:
		return ui_manager
	return null
