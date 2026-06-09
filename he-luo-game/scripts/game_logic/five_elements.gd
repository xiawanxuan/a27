extends RefCounted
class_name FiveElements

const METAL := "金"
const WOOD := "木"
const WATER := "水"
const FIRE := "火"
const EARTH := "土"

const ELEMENTS := [METAL, WOOD, WATER, FIRE, EARTH]

const ELEMENT_COLORS := {
	METAL: Color(0.9, 0.9, 0.95),
	WOOD: Color(0.3, 0.7, 0.3),
	WATER: Color(0.3, 0.5, 0.9),
	FIRE: Color(0.9, 0.3, 0.2),
	EARTH: Color(0.8, 0.6, 0.3)
}

static func get_conquers(element: String) -> String:
	match element:
		METAL:
			return WOOD
		WOOD:
			return EARTH
		EARTH:
			return WATER
		WATER:
			return FIRE
		FIRE:
			return METAL
	return ""

static func get_conquered_by(element: String) -> String:
	match element:
		METAL:
			return FIRE
		WOOD:
			return METAL
		WATER:
			return EARTH
		FIRE:
			return WATER
		EARTH:
			return WOOD
	return ""

static func does_conquer(element_a: String, element_b: String) -> bool:
	return get_conquers(element_a) == element_b

static func get_generates(element: String) -> String:
	match element:
		METAL:
			return WATER
		WATER:
			return WOOD
		WOOD:
			return FIRE
		FIRE:
			return EARTH
		EARTH:
			return METAL
	return ""

static func get_generated_by(element: String) -> String:
	match element:
		METAL:
			return EARTH
		WATER:
			return METAL
		WOOD:
			return WATER
		FIRE:
			return WOOD
		EARTH:
			return FIRE
	return ""

static func does_generate(element_a: String, element_b: String) -> bool:
	return get_generates(element_a) == element_b

static func get_element_color(element: String) -> Color:
	if ELEMENT_COLORS.has(element):
		return ELEMENT_COLORS[element]
	return Color.WHITE

static func get_all_elements() -> Array[String]:
	return ELEMENTS.duplicate()

static func get_conquest_pairs() -> Array[Dictionary]:
	var pairs: Array[Dictionary] = []
	for element in ELEMENTS:
		pairs.append({
			"conqueror": element,
			"conquered": get_conquers(element)
		})
	return pairs

static func verify_pair(conqueror: String, conquered: String) -> bool:
	return does_conquer(conqueror, conquered)

static func is_valid_element(element: String) -> bool:
	return ELEMENTS.has(element)

static func get_shuffled_elements() -> Array[String]:
	var shuffled: Array[String] = ELEMENTS.duplicate()
	shuffled.shuffle()
	return shuffled

static func get_river_map_pairs() -> Array[Dictionary]:
	var pairs: Array[Dictionary] = []
	pairs.append({"element": WATER, "number": 1, "position": "天一生水"})
	pairs.append({"element": FIRE, "number": 2, "position": "地二生火"})
	pairs.append({"element": WOOD, "number": 3, "position": "天三生木"})
	pairs.append({"element": METAL, "number": 4, "position": "地四生金"})
	pairs.append({"element": EARTH, "number": 5, "position": "天五生土"})
	pairs.append({"element": WATER, "number": 6, "position": "地六成水"})
	pairs.append({"element": FIRE, "number": 7, "position": "天七成火"})
	pairs.append({"element": WOOD, "number": 8, "position": "地八成木"})
	pairs.append({"element": METAL, "number": 9, "position": "天九成金"})
	pairs.append({"element": EARTH, "number": 10, "position": "地十成土"})
	return pairs
