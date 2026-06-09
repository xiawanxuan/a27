extends "res://scripts/drag_system/drop_slot.gd"
class_name GridSlot

var row: int = 0
var col: int = 0

func _ready() -> void:
	super._ready()
	custom_minimum_size = Vector2(80, 80)

func set_position_grid(r: int, c: int) -> void:
	row = r
	col = c
	slot_id = "slot_%d_%d" % [r, c]

func get_number_value() -> int:
	if current_item and current_item is NumberTile:
		return current_item.number_value
	return 0

func set_highlight(is_highlighted: bool) -> void:
	if is_highlighted:
		modulate = Color(1.2, 1.2, 0.8, 1.0)
	else:
		modulate = Color.WHITE

func show_correct_feedback() -> void:
	var tween := create_tween()
	tween.tween_property(self, "modulate", Color(0.5, 1.0, 0.5, 1.0), 0.3)
	tween.tween_property(self, "modulate", Color.WHITE, 0.3)

func show_wrong_feedback() -> void:
	var tween := create_tween()
	tween.tween_property(self, "modulate", Color(1.0, 0.5, 0.5, 1.0), 0.15)
	tween.tween_property(self, "modulate", Color.WHITE, 0.15)
	tween.tween_property(self, "modulate", Color(1.0, 0.5, 0.5, 1.0), 0.15)
	tween.tween_property(self, "modulate", Color.WHITE, 0.15)
