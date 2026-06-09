extends Control

@onready var elements_panel: VBoxContainer = $MainPanel/VBox/ContentHBox/ElementsPanel
@onready var pairs_container: VBoxContainer = $MainPanel/VBox/ContentHBox/PairsContainer
@onready var undo_button: Button = $MainPanel/VBox/ControlPanel/UndoButton
@onready var redo_button: Button = $MainPanel/VBox/ControlPanel/RedoButton
@onready var hint_button: Button = $MainPanel/VBox/ControlPanel/HintButton
@onready var bagua_button: Button = $MainPanel/VBox/ControlPanel/BaguaButton
@onready var reset_button: Button = $MainPanel/VBox/ControlPanel/ResetButton
@onready var back_button: Button = $MainPanel/VBox/ControlPanel/BackButton
@onready var title_label: Label = $MainPanel/VBox/TitleLabel
@onready var description_label: Label = $MainPanel/VBox/DescriptionLabel
@onready var progress_label: Label = $MainPanel/VBox/ProgressLabel

var element_tile_scene: PackedScene
var available_elements: Array[String] = []
var pair_slots: Array[Node] = []
var completed_pairs: int = 0
var total_pairs: int = 5
var is_completed: bool = false
var history: HistoryManager = null
var _suppress_history: bool = false

func _ready() -> void:
	element_tile_scene = preload("res://scenes/components/element_tile.tscn")
	_init_history()
	_init_elements()
	_init_pair_slots()
	_connect_signals()
	_load_game_state()
	_update_progress()
	_update_undo_buttons()

func _init_history() -> void:
	history = HistoryManager.new()
	add_child(history)
	history.history_changed.connect(_on_history_changed)

func _init_elements() -> void:
	available_elements = FiveElements.get_shuffled_elements()
	
	for element in available_elements:
		var tile = element_tile_scene.instantiate()
		tile.set_element(element)
		
		var bg_color = FiveElements.get_element_color(element)
		var stylebox := StyleBoxFlat.new()
		stylebox.bg_color = bg_color
		stylebox.border_color = bg_color.darkened(0.3)
		stylebox.border_width_left = 2
		stylebox.border_width_right = 2
		stylebox.border_width_top = 2
		stylebox.border_width_bottom = 2
		stylebox.corner_radius_top_left = 8
		stylebox.corner_radius_top_right = 8
		stylebox.corner_radius_bottom_right = 8
		stylebox.corner_radius_bottom_left = 8
		tile.add_theme_stylebox_override("panel", stylebox)
		
		tile.drag_ended.connect(_on_element_drag_ended)
		elements_panel.add_child(tile)

func _init_pair_slots() -> void:
	var conquerors: Array[String] = FiveElements.get_all_elements()
	conquerors.shuffle()
	
	for conqueror in conquerors:
		var pair_row := HBoxContainer.new()
		pair_row.theme_override_constants.separation = 10
		pair_row.alignment = BoxContainer.ALIGNMENT_CENTER
		
		var conqueror_slot := _create_conqueror_slot(conqueror)
		pair_row.add_child(conqueror_slot)
		
		var arrow_label := Label.new()
		arrow_label.text = "克"
		arrow_label.add_theme_color_override("font_color", Color(0.9, 0.3, 0.3))
		arrow_label.add_theme_font_size_override("font_size", 24)
		arrow_label.size_flags_horizontal = Control.SIZE_SHRINK_CENTER
		arrow_label.size_flags_vertical = Control.SIZE_SHRINK_CENTER
		pair_row.add_child(arrow_label)
		
		var conquered_slot := _create_conquered_slot(conqueror)
		pair_row.add_child(conquered_slot)
		
		pairs_container.add_child(pair_row)
		pair_slots.append(conquered_slot)

func _create_conqueror_slot(element: String) -> Control:
	var panel := Panel.new()
	panel.custom_minimum_size = Vector2(70, 70)
	panel.size_flags_horizontal = Control.SIZE_SHRINK_CENTER
	panel.size_flags_vertical = Control.SIZE_SHRINK_CENTER
	
	var stylebox := StyleBoxFlat.new()
	stylebox.bg_color = FiveElements.get_element_color(element)
	stylebox.border_color = FiveElements.get_element_color(element).darkened(0.3)
	stylebox.border_width_left = 2
	stylebox.border_width_right = 2
	stylebox.border_width_top = 2
	stylebox.border_width_bottom = 2
	stylebox.corner_radius_top_left = 8
	stylebox.corner_radius_top_right = 8
	stylebox.corner_radius_bottom_right = 8
	stylebox.corner_radius_bottom_left = 8
	panel.add_theme_stylebox_override("panel", stylebox)
	
	var label := Label.new()
	label.text = element
	label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	label.add_theme_color_override("font_color", Color.BLACK)
	label.add_theme_font_size_override("font_size", 24)
	label.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	panel.add_child(label)
	
	return panel

func _create_conquered_slot(conqueror: String) -> DropSlot:
	var slot := DropSlot.new()
	slot.custom_minimum_size = Vector2(70, 70)
	slot.size_flags_horizontal = Control.SIZE_SHRINK_CENTER
	slot.size_flags_vertical = Control.SIZE_SHRINK_CENTER
	slot.slot_id = "slot_" + conqueror
	slot.accept_types = ["element"]
	
	var bg := ColorRect.new()
	bg.color = Color(0.2, 0.2, 0.3, 0.8)
	bg.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	slot.add_child(bg)
	
	var hint_label := Label.new()
	hint_label.text = "?"
	hint_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	hint_label.vertical_alignment = VERTICAL_ALIGNMENT_CENTER
	hint_label.add_theme_color_override("font_color", Color(0.7, 0.7, 0.7))
	hint_label.add_theme_font_size_override("font_size", 24)
	hint_label.set_anchors_and_offsets_preset(Control.PRESET_FULL_RECT)
	slot.add_child(hint_label)
	
	slot.set_meta("conqueror", conqueror)
	slot.item_dropped.connect(_on_pair_slot_dropped)
	
	return slot

func _connect_signals() -> void:
	undo_button.pressed.connect(_on_undo_pressed)
	redo_button.pressed.connect(_on_redo_pressed)
	hint_button.pressed.connect(_on_hint_pressed)
	bagua_button.pressed.connect(_on_bagua_pressed)
	reset_button.pressed.connect(_on_reset_pressed)
	back_button.pressed.connect(_on_back_pressed)

func _on_element_drag_ended(item: DraggableItem, dropped: bool) -> void:
	pass

func _on_pair_slot_dropped(item: DraggableItem, slot: DropSlot) -> void:
	if not item is ElementTile:
		return
	
	var conqueror: String = slot.get_meta("conqueror", "")
	var element: String = item.get_element()
	var correct_element: String = FiveElements.get_conquers(conqueror)
	
	if element == correct_element:
		_on_correct_pair(slot, item)
	else:
		_on_wrong_pair(slot, item)

func _on_correct_pair(slot: DropSlot, item: ElementTile) -> void:
	var element_name: String = item.element_name
	var conqueror_name: String = slot.get_meta("conqueror", "")
	
	if not _suppress_history:
		var action_data := {
			"type": "he_tu_pair",
			"conqueror": conqueror_name,
			"conquered": element_name,
			"correct": true
		}
		history.push_action(action_data)
		_update_undo_buttons()
	
	completed_pairs += 1
	item.set_correct_state(true)
	slot.modulate = Color(1.2, 1.2, 1.0, 1.0)
	
	var hint_label: Label = slot.get_child(1)
	if hint_label:
		hint_label.visible = false
	
	_update_progress()
	_save_game_state()
	_check_completion()

func _on_wrong_pair(slot: DropSlot, item: ElementTile) -> void:
	slot.remove_item()
	
	var tween := create_tween()
	tween.set_parallel(true)
	tween.tween_property(item, "modulate", Color(1.0, 0.5, 0.5, 1.0), 0.1)
	tween.chain().tween_property(item, "modulate", Color.WHITE, 0.1)
	
	item.reset_to_original()

func _update_progress() -> void:
	progress_label.text = "进度: %d / %d" % [completed_pairs, total_pairs]

func _check_completion() -> void:
	if completed_pairs >= total_pairs:
		_on_victory()

func _on_victory() -> void:
	is_completed = true
	
	if GameManager:
		GameManager.complete_level("he_tu")
	
	var ui_manager = _get_ui_manager()
	if ui_manager:
		ui_manager.show_victory("河图五行完成！\n相克关系全部正确！")
		if not ui_manager.victory_continue_pressed.is_connected(_on_victory_continue):
			ui_manager.victory_continue_pressed.connect(_on_victory_continue)
		if not ui_manager.victory_replay_pressed.is_connected(_on_victory_replay):
			ui_manager.victory_replay_pressed.connect(_on_victory_replay)
	
	_save_game_state()

func _on_victory_continue() -> void:
	_on_back_pressed()

func _on_victory_replay() -> void:
	_on_reset_pressed()

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
	_update_progress()

func _on_redo_pressed() -> void:
	if not history or not history.can_redo():
		return
	
	var action = history.redo()
	if action.is_empty():
		return
	
	_apply_history_action(action, false)
	_save_game_state()
	_update_progress()

func _apply_history_action(action: Dictionary, is_undo: bool) -> void:
	_suppress_history = true
	
	if action.get("type", "") == "he_tu_pair" and action.get("correct", false):
		var conquered: String = action.get("conquered", "")
		var conqueror: String = action.get("conqueror", "")
		
		if is_undo:
			_undo_pair(conquered, conqueror)
		else:
			_redo_pair(conquered, conqueror)
	
	_suppress_history = false

func _undo_pair(conquered: String, conqueror: String) -> void:
	for slot in pair_slots:
		if slot is DropSlot:
			var slot_conqueror: String = slot.get_meta("conqueror", "")
			if slot_conqueror == conqueror and slot.has_item():
				var item: ElementTile = slot.current_item
				if item and item.element_name == conquered:
					var removed_item = slot.remove_item()
					if removed_item:
						removed_item.set_correct_state(false)
						removed_item.reset_to_original()
					
					slot.modulate = Color.WHITE
					
					var hint_label: Label = slot.get_child(1)
					if hint_label:
						hint_label.visible = true
					
					completed_pairs -= 1
					is_completed = false
					break

func _redo_pair(conquered: String, conqueror: String) -> void:
	for slot in pair_slots:
		if slot is DropSlot:
			var slot_conqueror: String = slot.get_meta("conqueror", "")
			if slot_conqueror == conqueror and not slot.has_item():
				for tile in elements_panel.get_children():
					if tile is ElementTile and tile.element_name == conquered:
						slot.accept_item(tile)
						tile.set_correct_state(true)
						slot.modulate = Color(1.2, 1.2, 1.0, 1.0)
						
						var hint_label: Label = slot.get_child(1)
						if hint_label:
							hint_label.visible = false
						
						completed_pairs += 1
						_check_completion()
						break
				break

func _on_bagua_pressed() -> void:
	if not is_completed:
		var ui_manager = _get_ui_manager()
		if ui_manager:
			ui_manager.show_hint("请先完成五行相克配对再推演八卦！")
		return
	
	_show_bagua_dialog()

func _show_bagua_dialog() -> void:
	var dialog := AcceptDialog.new()
	dialog.title = "卦象推演"
	dialog.ok_button_text = "关闭"
	dialog.custom_minimum_size = Vector2(500, 500)
	
	var vbox := VBoxContainer.new()
	vbox.add_theme_constant_override("separation", 10)
	
	var title_label := Label.new()
	title_label.text = "五行与八卦对应"
	title_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	title_label.add_theme_font_size_override("font_size", 20)
	vbox.add_child(title_label)
	
	var desc_label := Label.new()
	desc_label.text = "点击卦象查看详细解释"
	desc_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	desc_label.modulate = Color(0.7, 0.7, 0.7)
	vbox.add_child(desc_label)
	
	var scroll := ScrollContainer.new()
	scroll.custom_minimum_size = Vector2(450, 380)
	vbox.add_child(scroll)
	
	var content_vbox := VBoxContainer.new()
	content_vbox.add_theme_constant_override("separation", 10)
	scroll.add_child(content_vbox)
	
	var elements = FiveElements.get_all_elements()
	for element in elements:
		var element_panel := _create_element_bagua_panel(element)
		content_vbox.add_child(element_panel)
	
	dialog.add_child(vbox)
	add_child(dialog)
	dialog.popup_centered()

func _create_element_bagua_panel(element: String) -> Control:
	var panel := PanelContainer.new()
	
	var stylebox := StyleBoxFlat.new()
	var color = FiveElements.get_element_color(element)
	stylebox.bg_color = color
	stylebox.bg_color.a = 0.3
	stylebox.border_width_left = 2
	stylebox.border_width_right = 2
	stylebox.border_width_top = 2
	stylebox.border_width_bottom = 2
	stylebox.border_color = color.darkened(0.2)
	stylebox.corner_radius_top_left = 8
	stylebox.corner_radius_top_right = 8
	stylebox.corner_radius_bottom_right = 8
	stylebox.corner_radius_bottom_left = 8
	panel.add_theme_stylebox_override("panel", stylebox)
	
	var vbox := VBoxContainer.new()
	vbox.add_theme_constant_override("separation", 8)
	vbox.offset_left = 10
	vbox.offset_top = 8
	vbox.offset_right = -10
	vbox.offset_bottom = -8
	panel.add_child(vbox)
	
	var element_label := Label.new()
	element_label.text = element + "行"
	element_label.add_theme_font_size_override("font_size", 18)
	element_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	vbox.add_child(element_label)
	
	var trigrams_hbox := HBoxContainer.new()
	trigrams_hbox.alignment = BoxContainer.ALIGNMENT_CENTER
	trigrams_hbox.add_theme_constant_override("separation", 15)
	vbox.add_child(trigrams_hbox)
	
	var trigram_list: Array[String] = []
	for trigram in BaguaSystem.get_all_trigrams():
		if BaguaSystem.get_trigram_element(trigram) == element:
			trigram_list.append(trigram)
	
	for trigram_name in trigram_list:
		var trigram_panel := PanelContainer.new()
		trigram_panel.custom_minimum_size = Vector2(90, 100)
		
		var t_stylebox := StyleBoxFlat.new()
		t_stylebox.bg_color = Color.WHITE
		t_stylebox.bg_color.a = 0.85
		t_stylebox.corner_radius_top_left = 6
		t_stylebox.corner_radius_top_right = 6
		t_stylebox.corner_radius_bottom_right = 6
		t_stylebox.corner_radius_bottom_left = 6
		trigram_panel.add_theme_stylebox_override("panel", t_stylebox)
		
		var t_vbox := VBoxContainer.new()
		t_vbox.alignment = BoxContainer.ALIGNMENT_CENTER
		t_vbox.add_theme_constant_override("separation", 2)
		
		var symbol_label := Label.new()
		symbol_label.text = BaguaSystem.get_trigram_symbol(trigram_name)
		symbol_label.add_theme_font_size_override("font_size", 36)
		symbol_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
		symbol_label.add_theme_color_override("font_color", Color.BLACK)
		t_vbox.add_child(symbol_label)
		
		var name_label := Label.new()
		name_label.text = trigram_name
		name_label.add_theme_font_size_override("font_size", 14)
		name_label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
		name_label.add_theme_color_override("font_color", Color.BLACK)
		t_vbox.add_child(name_label)
		
		trigram_panel.add_child(t_vbox)
		trigram_panel.mouse_filter = Control.MOUSE_FILTER_STOP
		trigram_panel.gui_input.connect(_on_trigram_gui_input.bind(trigram_name))
		
		trigrams_hbox.add_child(trigram_panel)
	
	return panel

func _on_trigram_gui_input(event: InputEvent, trigram_name: String) -> void:
	if event is InputEventMouseButton and event.button_index == MOUSE_BUTTON_LEFT and event.pressed:
		_show_trigram_explanation(trigram_name)

func _show_trigram_explanation(trigram_name: String) -> void:
	var explanation = BaguaSystem.get_trigram_explanation(trigram_name)
	if explanation.is_empty():
		return
	
	var dialog := AcceptDialog.new()
	dialog.title = "%s %s - 卦象详解" % [BaguaSystem.get_trigram_symbol(trigram_name), trigram_name]
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
	for slot in pair_slots:
		if slot is DropSlot and not slot.has_item():
			var conqueror: String = slot.get_meta("conqueror", "")
			var correct_element: String = FiveElements.get_conquers(conqueror)
			
			var ui_manager = _get_ui_manager()
			if ui_manager:
				ui_manager.show_hint("%s 克 %s" % [conqueror, correct_element])
			
			var tween := create_tween()
			slot.modulate = Color(1.0, 1.0, 0.5, 1.0)
			tween.tween_property(slot, "modulate", Color.WHITE, 1.0)
			return

func _on_reset_pressed() -> void:
	for slot in pair_slots:
		if slot is DropSlot and slot.has_item():
			var item = slot.remove_item()
			if item:
				item.reset_to_original()
				item.set_correct_state(false)
			
			slot.modulate = Color.WHITE
			var hint_label: Label = slot.get_child(1)
			if hint_label:
				hint_label.visible = true
	
	completed_pairs = 0
	is_completed = false
	if history:
		history.clear()
	_update_progress()
	_update_undo_buttons()

func _on_back_pressed() -> void:
	_save_game_state()
	if GameManager:
		GameManager.goto_main_menu()

func _save_game_state() -> void:
	if GameManager and GameManager.save_system:
		var pairs_state: Array = []
		for slot in pair_slots:
			if slot is DropSlot:
				var conqueror: String = slot.get_meta("conqueror", "")
				var element: String = ""
				if slot.has_item() and slot.current_item is ElementTile:
					element = slot.current_item.get_element()
				pairs_state.append({"conqueror": conqueror, "element": element})
		
		var remaining_elements: Array[String] = []
		for child in elements_panel.get_children():
			if child is ElementTile:
				remaining_elements.append(child.get_element())
		
		var data := {
			"type": "he_tu",
			"pairs": pairs_state,
			"remaining_elements": remaining_elements,
			"completed": is_completed,
			"completed_pairs": completed_pairs
		}
		GameManager.save_system.auto_save(data)

func _load_game_state() -> void:
	if not GameManager or not GameManager.save_system:
		return
	
	var data = GameManager.save_system.load_he_tu_state()
	if data.is_empty():
		return
	
	var pairs_state = data.get("pairs", [])
	var remaining_elements: Array[String] = data.get("remaining_elements", [])
	var saved_completed: bool = data.get("completed", false)
	var saved_completed_pairs: int = data.get("completed_pairs", 0)
	
	if pairs_state.is_empty():
		return
	
	for child in elements_panel.get_children():
		if is_instance_valid(child):
			child.queue_free()
	
	for slot in pair_slots:
		if is_instance_valid(slot):
			slot.queue_free()
	pair_slots.clear()
	
	for child in pairs_container.get_children():
		if is_instance_valid(child):
			child.queue_free()
	
	completed_pairs = 0
	is_completed = false
	
	for pair_data in pairs_state:
		var conqueror: String = pair_data.get("conqueror", "")
		var element: String = pair_data.get("element", "")
		
		var pair_row := HBoxContainer.new()
		pair_row.theme_override_constants.separation = 10
		pair_row.alignment = BoxContainer.ALIGNMENT_CENTER
		
		var conqueror_slot := _create_conqueror_slot(conqueror)
		pair_row.add_child(conqueror_slot)
		
		var arrow_label := Label.new()
		arrow_label.text = "克"
		arrow_label.add_theme_color_override("font_color", Color(0.9, 0.3, 0.3))
		arrow_label.add_theme_font_size_override("font_size", 24)
		arrow_label.size_flags_horizontal = Control.SIZE_SHRINK_CENTER
		arrow_label.size_flags_vertical = Control.SIZE_SHRINK_CENTER
		pair_row.add_child(arrow_label)
		
		var conquered_slot := _create_conquered_slot(conqueror)
		pair_row.add_child(conquered_slot)
		
		pairs_container.add_child(pair_row)
		pair_slots.append(conquered_slot)
		
		if element != "" and remaining_elements.has(element):
			remaining_elements.erase(element)
			var tile = element_tile_scene.instantiate()
			tile.set_element(element)
			tile.drag_ended.connect(_on_element_drag_ended)
			
			var bg_color = FiveElements.get_element_color(element)
			var stylebox := StyleBoxFlat.new()
			stylebox.bg_color = bg_color
			stylebox.border_color = bg_color.darkened(0.3)
			stylebox.border_width_left = 2
			stylebox.border_width_right = 2
			stylebox.border_width_top = 2
			stylebox.border_width_bottom = 2
			stylebox.corner_radius_top_left = 8
			stylebox.corner_radius_top_right = 8
			stylebox.corner_radius_bottom_right = 8
			stylebox.corner_radius_bottom_left = 8
			tile.add_theme_stylebox_override("panel", stylebox)
			
			conquered_slot.accept_item(tile)
			tile.set_correct_state(true)
			completed_pairs += 1
			
			var hint_label: Label = conquered_slot.get_child(1)
			if hint_label:
				hint_label.visible = false
	
	for element in remaining_elements:
		var tile = element_tile_scene.instantiate()
		tile.set_element(element)
		tile.drag_ended.connect(_on_element_drag_ended)
		
		var bg_color = FiveElements.get_element_color(element)
		var stylebox := StyleBoxFlat.new()
		stylebox.bg_color = bg_color
		stylebox.border_color = bg_color.darkened(0.3)
		stylebox.border_width_left = 2
		stylebox.border_width_right = 2
		stylebox.border_width_top = 2
		stylebox.border_width_bottom = 2
		stylebox.corner_radius_top_left = 8
		stylebox.corner_radius_top_right = 8
		stylebox.corner_radius_bottom_right = 8
		stylebox.corner_radius_bottom_left = 8
		tile.add_theme_stylebox_override("panel", stylebox)
		
		elements_panel.add_child(tile)
	
	if saved_completed or saved_completed_pairs >= total_pairs:
		is_completed = true
		completed_pairs = total_pairs
	
	_update_progress()

func _get_ui_manager() -> UIManager:
	var ui_manager = get_tree().get_first_node_in_group("ui_manager")
	if ui_manager and ui_manager is UIManager:
		return ui_manager
	return null
