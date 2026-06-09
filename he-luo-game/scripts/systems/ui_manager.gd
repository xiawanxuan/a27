extends CanvasLayer
class_name UIManager

signal hint_shown(message: String)
signal victory_started()
signal victory_finished()
signal victory_continue_pressed()
signal victory_replay_pressed()

@onready var hint_label: Label = $HintPanel/HintLabel
@onready var hint_panel: Panel = $HintPanel
@onready var victory_panel: Panel = $VictoryPanel
@onready var victory_label: Label = $VictoryPanel/VBox/VictoryLabel
@onready var victory_buttons: HBoxContainer = $VictoryPanel/VBox/ButtonsContainer
@onready var continue_button: Button = $VictoryPanel/VBox/ButtonsContainer/ContinueButton
@onready var replay_button: Button = $VictoryPanel/VBox/ButtonsContainer/ReplayButton
@onready var victory_tween: Tween = null

var hint_timer: Timer = null
var is_victory_playing: bool = false
var particles: Array = []

func _ready() -> void:
	add_to_group("ui_manager")
	
	hint_timer = Timer.new()
	hint_timer.wait_time = 3.0
	hint_timer.one_shot = true
	hint_timer.timeout.connect(_on_hint_timeout)
	add_child(hint_timer)
	
	if hint_panel:
		hint_panel.visible = false
	if victory_panel:
		victory_panel.visible = false
		if victory_buttons:
			victory_buttons.visible = false
		if continue_button:
			continue_button.pressed.connect(_on_victory_continue)
		if replay_button:
			replay_button.pressed.connect(_on_victory_replay)

func show_hint(message: String, duration: float = 3.0) -> void:
	if hint_label:
		hint_label.text = message
	if hint_panel:
		hint_panel.visible = true
		_play_hint_show_animation()
	
	hint_timer.wait_time = duration
	hint_timer.start()
	
	hint_shown.emit(message)

func _play_hint_show_animation() -> void:
	if not hint_panel:
		return
	var tween := create_tween()
	hint_panel.modulate.a = 0.0
	hint_panel.position.y = -20.0
	tween.tween_property(hint_panel, "modulate:a", 1.0, 0.3)
	tween.parallel().tween_property(hint_panel, "position:y", 0.0, 0.3)

func _on_hint_timeout() -> void:
	if hint_panel:
		_play_hint_hide_animation()

func _play_hint_hide_animation() -> void:
	if not hint_panel:
		return
	var tween := create_tween()
	tween.tween_property(hint_panel, "modulate:a", 0.0, 0.3)
	tween.parallel().tween_property(hint_panel, "position:y", -20.0, 0.3)
	tween.tween_callback(hint_panel.hide)

func show_victory(message: String = "恭喜通关！", show_buttons: bool = true) -> void:
	if is_victory_playing:
		return
	
	is_victory_playing = true
	victory_started.emit()
	
	if victory_label:
		victory_label.text = message
	
	if victory_buttons:
		victory_buttons.visible = show_buttons
	
	if victory_panel:
		victory_panel.visible = true
		_play_victory_animation()

func _play_victory_animation() -> void:
	if not victory_panel:
		return
	
	victory_panel.modulate.a = 0.0
	victory_panel.scale = Vector2(0.5, 0.5)
	victory_panel.position = get_viewport_rect().size / 2
	
	var tween := create_tween()
	tween.set_parallel(true)
	tween.tween_property(victory_panel, "modulate:a", 1.0, 0.5)
	tween.tween_property(victory_panel, "scale", Vector2(1.2, 1.2), 0.5)
	tween.chain().tween_property(victory_panel, "scale", Vector2(1.0, 1.0), 0.3)
	
	_play_confetti_particles()

func _play_confetti_particles() -> void:
	var colors := [
		Color(0.95, 0.3, 0.3),
		Color(0.3, 0.9, 0.4),
		Color(0.3, 0.5, 0.95),
		Color(0.95, 0.85, 0.3),
		Color(0.9, 0.3, 0.9),
		Color(0.3, 0.9, 0.9)
	]
	
	for i in range(40):
		var particle := ColorRect.new()
		particle.size = Vector2(6 + randf() * 6, 6 + randf() * 6)
		particle.color = colors[i % colors.size()]
		particle.position = get_viewport_rect().size / 2
		particle.rotation = randf() * PI
		add_child(particle)
		particles.append(particle)
		
		var tween := create_tween()
		var angle := randf() * TAU
		var distance := 150.0 + randf() * 250.0
		var start_pos = particle.position
		var target_pos = start_pos + Vector2(cos(angle), sin(angle)) * distance
		target_pos.y += 50.0 + randf() * 100.0
		
		tween.set_parallel(true)
		tween.tween_property(particle, "position", target_pos, 1.5 + randf() * 1.0)
		tween.tween_property(particle, "rotation", randf() * PI * 2, 1.5 + randf() * 1.0)
		tween.tween_property(particle, "modulate:a", 0.0, 1.5 + randf() * 1.0)
		tween.tween_property(particle, "scale", Vector2(0.2, 0.2), 1.5 + randf() * 1.0)
		tween.tween_callback(particle.queue_free)

func _on_victory_continue() -> void:
	hide_victory()
	victory_continue_pressed.emit()

func _on_victory_replay() -> void:
	hide_victory()
	victory_replay_pressed.emit()

func hide_victory() -> void:
	if victory_panel:
		victory_panel.visible = false
	if victory_buttons:
		victory_buttons.visible = false
	is_victory_playing = false
	_clear_particles()
	victory_finished.emit()

func _clear_particles() -> void:
	for p in particles:
		if is_instance_valid(p):
			p.queue_free()
	particles.clear()

func show_confirmation(message: String, on_confirm: Callable, on_cancel: Callable = Callable()) -> void:
	var confirm_panel := ConfirmationDialog.new()
	confirm_panel.title = "确认"
	confirm_panel.dialog_text = message
	confirm_panel.ok_button_text = "确定"
	confirm_panel.cancel_button_text = "取消"
	confirm_panel.confirmed.connect(on_confirm)
	if on_cancel.is_valid():
		confirm_panel.canceled.connect(on_cancel)
	add_child(confirm_panel)
	confirm_panel.popup_centered()

func show_message_box(message: String, title: String = "提示") -> void:
	var dialog := AcceptDialog.new()
	dialog.title = title
	dialog.dialog_text = message
	dialog.ok_button_text = "确定"
	add_child(dialog)
	dialog.popup_centered()
