extends Control
class_name DraggableItem

signal drag_started(item: DraggableItem)
signal drag_ended(item: DraggableItem, dropped: bool)
signal dropped_on_slot(slot: DropSlot)

var item_data: Variant = null
var original_parent: Node = null
var original_position: Vector2 = Vector2.ZERO
var is_dragging: bool = false
var drag_offset: Vector2 = Vector2.ZERO
var _active_tween: Tween = null

@export var item_value: String = ""
@export var item_color: Color = Color.WHITE

func _ready() -> void:
	mouse_filter = Control.MOUSE_FILTER_STOP
	original_parent = get_parent()

func _gui_input(event: InputEvent) -> void:
	if event is InputEventMouseButton:
		if event.button_index == MOUSE_BUTTON_LEFT:
			if event.pressed:
				_start_drag(event.position)
			elif is_dragging:
				_end_drag()
	elif event is InputEventMouseMotion and is_dragging:
		_update_drag_position(event.position)

func _start_drag(local_pos: Vector2) -> void:
	if is_dragging:
		return
	is_dragging = true
	original_position = position
	drag_offset = local_pos
	
	var viewport := get_viewport()
	if viewport:
		var global_pos = get_global_position()
		original_parent = get_parent()
		remove_from_parent()
		viewport.add_child(self)
		set_global_position(global_pos)
		z_index = 100
	
	_play_drag_start_animation()
	drag_started.emit(self)

func _stop_active_tween() -> void:
	if _active_tween and is_instance_valid(_active_tween):
		_active_tween.kill()
	_active_tween = null

func _play_drag_start_animation() -> void:
	_stop_active_tween()
	scale = Vector2(1.0, 1.0)
	_active_tween = create_tween()
	_active_tween.set_parallel(true)
	_active_tween.tween_property(self, "scale", Vector2(1.15, 1.15), 0.1)
	_active_tween.tween_property(self, "modulate:a", 0.9, 0.1)
	_active_tween.finished.connect(_on_tween_finished)

func _update_drag_position(local_pos: Vector2) -> void:
	var mouse_pos: Vector2 = get_global_mouse_position()
	set_global_position(mouse_pos - size / 2)

func _end_drag() -> void:
	if not is_dragging:
		return
	is_dragging = false
	
	var dropped := false
	var viewport := get_viewport()
	if viewport:
		var mouse_pos: Vector2 = get_global_mouse_position()
		var slots: Array = get_tree().get_nodes_in_group("drop_slots")
		for slot in slots:
			if slot and slot is DropSlot and is_instance_valid(slot):
				if slot.is_point_in_slot(mouse_pos):
					if slot.can_accept_item(self):
						_drop_on_slot(slot)
						dropped = true
						break
	
	if not dropped:
		_return_to_original()
	
	_play_drag_end_animation(dropped)
	z_index = 0
	drag_ended.emit(self, dropped)

func _play_drag_end_animation(dropped: bool) -> void:
	_stop_active_tween()
	_active_tween = create_tween()
	_active_tween.set_parallel(true)
	if dropped:
		_active_tween.tween_property(self, "scale", Vector2(0.9, 0.9), 0.08)
		_active_tween.chain().tween_property(self, "scale", Vector2(1.0, 1.0), 0.08)
	else:
		_active_tween.tween_property(self, "scale", Vector2(1.0, 1.0), 0.15)
		_active_tween.tween_property(self, "modulate:a", 1.0, 0.15)
	_active_tween.finished.connect(_on_tween_finished)

func _on_tween_finished() -> void:
	_active_tween = null

func _drop_on_slot(slot: DropSlot) -> void:
	if original_parent and original_parent is DropSlot and original_parent != slot:
		original_parent.remove_item()
	
	if original_parent and original_parent.has_method("_on_item_dropped_from"):
		original_parent._on_item_dropped_from(self)
	
	slot.accept_item(self)
	dropped_on_slot.emit(slot)

func _return_to_original() -> void:
	if original_parent and is_instance_valid(original_parent):
		if get_parent() != original_parent:
			remove_from_parent()
			original_parent.add_child(self)
		position = original_position

func set_item_data(data: Variant) -> void:
	item_data = data

func get_item_data() -> Variant:
	return item_data

func reset_to_original() -> void:
	_stop_active_tween()
	if original_parent and is_instance_valid(original_parent):
		if get_parent() != original_parent:
			if get_parent():
				remove_from_parent()
			original_parent.add_child(self)
		position = original_position
		scale = Vector2.ONE
		modulate = Color.WHITE
	original_parent = get_parent()
