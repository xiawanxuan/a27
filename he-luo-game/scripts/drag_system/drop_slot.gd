extends Control
class_name DropSlot

signal item_dropped(item: DraggableItem, slot: DropSlot)
signal item_removed(item: DraggableItem, slot: DropSlot)

var current_item: DraggableItem = null
var slot_data: Variant = null

@export var slot_id: String = ""
@export var accept_types: Array[String] = []
@export var highlight_color: Color = Color(0.3, 0.8, 0.3, 0.3)
@export var normal_color: Color = Color(0.2, 0.2, 0.3, 0.5)

func _ready() -> void:
	add_to_group("drop_slots")
	mouse_filter = Control.MOUSE_FILTER_PASS

func is_point_in_slot(global_point: Vector2) -> bool:
	var rect := Rect2(get_global_position(), size)
	return rect.has_point(global_point)

func can_accept_item(item: DraggableItem) -> bool:
	if current_item != null:
		return false
	if accept_types.is_empty():
		return true
	if item.has_method("get_item_type"):
		return accept_types.has(item.get_item_type())
	return true

func accept_item(item: DraggableItem) -> void:
	if current_item and current_item != item:
		current_item.reset_to_original()
	
	current_item = item
	item.remove_from_parent()
	add_child(item)
	item.position = Vector2.ZERO
	item.size = size
	item.original_parent = self
	item.original_position = Vector2.ZERO
	
	item_dropped.emit(item, self)
	_on_item_accepted(item)

func remove_item() -> DraggableItem:
	var item = current_item
	if current_item:
		current_item = null
		item_removed.emit(item, self)
		_on_item_removed(item)
	return item

func get_current_item() -> DraggableItem:
	return current_item

func has_item() -> bool:
	return current_item != null

func clear_slot() -> void:
	if current_item:
		current_item.queue_free()
		current_item = null

func _on_item_accepted(item: DraggableItem) -> void:
	pass

func _on_item_removed(item: DraggableItem) -> void:
	pass
