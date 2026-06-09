extends Node
class_name HistoryManager

signal history_changed(can_undo: bool, can_redo: bool)
signal action_undone(action_data: Dictionary)
signal action_redone(action_data: Dictionary)

const MAX_HISTORY := 50

var _undo_stack: Array[Dictionary] = []
var _redo_stack: Array[Dictionary] = []

func _ready() -> void:
	pass

func push_action(action_data: Dictionary) -> void:
	_undo_stack.append(action_data.duplicate(true))
	
	if _undo_stack.size() > MAX_HISTORY:
		_undo_stack.pop_front()
	
	_redo_stack.clear()
	_emit_history_changed()

func undo() -> Dictionary:
	if _undo_stack.is_empty():
		return {}
	
	var action_data: Dictionary = _undo_stack.pop_back()
	_redo_stack.append(action_data.duplicate(true))
	
	_emit_history_changed()
	action_undone.emit(action_data)
	return action_data

func redo() -> Dictionary:
	if _redo_stack.is_empty():
		return {}
	
	var action_data: Dictionary = _redo_stack.pop_back()
	_undo_stack.append(action_data.duplicate(true))
	
	_emit_history_changed()
	action_redone.emit(action_data)
	return action_data

func can_undo() -> bool:
	return not _undo_stack.is_empty()

func can_redo() -> bool:
	return not _redo_stack.is_empty()

func clear() -> void:
	_undo_stack.clear()
	_redo_stack.clear()
	_emit_history_changed()

func get_undo_count() -> int:
	return _undo_stack.size()

func get_redo_count() -> int:
	return _redo_stack.size()

func _emit_history_changed() -> void:
	history_changed.emit(can_undo(), can_redo())

func peek_undo() -> Dictionary:
	if _undo_stack.is_empty():
		return {}
	return _undo_stack[_undo_stack.size() - 1]

func peek_redo() -> Dictionary:
	if _redo_stack.is_empty():
		return {}
	return _redo_stack[_redo_stack.size() - 1]
