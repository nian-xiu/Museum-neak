#!/usr/bin/env python3
"""Generate anti-aliased 8-direction actor sprite atlases for Museum Heist 2D.

Each atlas contains 3 action blocks (idle, walk, run), 8 direction rows per
block and 8 animation frames per row. The output is deterministic and can be
regenerated whenever the actor palette or silhouettes change.
"""
from __future__ import annotations

import argparse
import math
from pathlib import Path
from typing import Iterable

from PIL import Image, ImageDraw

CELL_W = 96
CELL_H = 128
FRAMES = 8
DIRECTIONS = (
    (1.0, 0.0),
    (math.sqrt(0.5), math.sqrt(0.5)),
    (0.0, 1.0),
    (-math.sqrt(0.5), math.sqrt(0.5)),
    (-1.0, 0.0),
    (-math.sqrt(0.5), -math.sqrt(0.5)),
    (0.0, -1.0),
    (math.sqrt(0.5), -math.sqrt(0.5)),
)
ACTIONS = ("idle", "walk", "run")
SS = 3

ACTORS = {
    "city_explorer": {
        "kind": "human", "variant": 0,
        "primary": (38, 111, 190), "secondary": (232, 238, 241),
        "accent": (238, 128, 54), "dark": (22, 28, 34),
        "skin": (220, 174, 132), "pants": (18, 28, 42), "shoes": (28, 35, 44),
        "hair": (24, 35, 53),
    },
    "tech_scout": {
        "kind": "human", "variant": 1,
        "primary": (34, 52, 66), "secondary": (102, 196, 214),
        "accent": (202, 216, 222), "dark": (26, 34, 43),
        "skin": (214, 169, 126), "pants": (42, 48, 58), "shoes": (68, 86, 96),
        "hair": (22, 39, 49),
    },
    "museum_researcher": {
        "kind": "human", "variant": 2,
        "primary": (116, 91, 58), "secondary": (214, 197, 162),
        "accent": (54, 97, 72), "dark": (72, 45, 28),
        "skin": (208, 162, 118), "pants": (62, 72, 64), "shoes": (63, 48, 34),
        "hair": (104, 55, 31),
    },
    "street_player": {
        "kind": "human", "variant": 3,
        "primary": (92, 48, 150), "secondary": (30, 31, 42),
        "accent": (236, 210, 56), "dark": (31, 24, 36),
        "skin": (218, 166, 124), "pants": (36, 39, 48), "shoes": (245, 218, 71),
        "hair": (37, 28, 49),
    },
    "fantasy_guardian": {
        "kind": "human", "variant": 4,
        "primary": (226, 222, 200), "secondary": (174, 151, 216),
        "accent": (124, 196, 222), "dark": (65, 58, 88),
        "skin": (225, 179, 137), "pants": (82, 74, 116), "shoes": (236, 226, 192),
        "hair": (238, 237, 224),
    },
    "guard": {
        "kind": "human", "variant": 10,
        "primary": (132, 49, 53), "secondary": (183, 68, 70),
        "accent": (218, 176, 70), "dark": (61, 25, 29),
        "skin": (207, 155, 116), "pants": (47, 38, 42), "shoes": (24, 28, 31),
        "hair": (42, 31, 28),
    },
    "staff": {
        "kind": "human", "variant": 11,
        "primary": (48, 92, 105), "secondary": (228, 236, 230),
        "accent": (226, 194, 75), "dark": (27, 48, 58),
        "skin": (222, 174, 130), "pants": (34, 40, 44), "shoes": (24, 29, 33),
        "hair": (45, 36, 30),
    },
    "robot": {
        "kind": "robot", "variant": 20,
        "primary": (104, 125, 136), "secondary": (45, 57, 64),
        "accent": (110, 214, 150), "dark": (26, 35, 40),
        "skin": (0, 0, 0), "pants": (42, 52, 58), "shoes": (31, 39, 44),
        "hair": (0, 0, 0),
    },
}


def clamp_channel(value: float) -> int:
    return max(0, min(255, int(round(value))))


def shade(color: tuple[int, int, int], factor: float) -> tuple[int, int, int, int]:
    return tuple(clamp_channel(channel * factor) for channel in color) + (255,)


def rgba(color: tuple[int, int, int], alpha: int = 255) -> tuple[int, int, int, int]:
    return color + (alpha,)


def sp(point: tuple[float, float]) -> tuple[int, int]:
    return (round(point[0] * SS), round(point[1] * SS))


def sbox(box: tuple[float, float, float, float]) -> tuple[int, int, int, int]:
    return tuple(round(value * SS) for value in box)


def rounded_line(draw: ImageDraw.ImageDraw, points: Iterable[tuple[float, float]],
                 fill: tuple[int, int, int, int], width: float) -> None:
    pts = [sp(point) for point in points]
    px_width = max(1, round(width * SS))
    draw.line(pts, fill=fill, width=px_width, joint="curve")
    radius = px_width // 2
    for x, y in pts:
        draw.ellipse((x - radius, y - radius, x + radius, y + radius), fill=fill)


def limb(draw: ImageDraw.ImageDraw, points: Iterable[tuple[float, float]],
         color: tuple[int, int, int], width: float, outline=(18, 22, 27)) -> None:
    pts = list(points)
    rounded_line(draw, pts, rgba(outline), width + 2.5)
    rounded_line(draw, pts, rgba(color), width)


def joint(draw: ImageDraw.ImageDraw, point: tuple[float, float], radius: float,
          color: tuple[int, int, int], outline=(18, 22, 27)) -> None:
    x, y = point
    draw.ellipse(sbox((x - radius - 1.2, y - radius - 1.2,
                       x + radius + 1.2, y + radius + 1.2)), fill=rgba(outline))
    draw.ellipse(sbox((x - radius, y - radius, x + radius, y + radius)), fill=rgba(color))


def polygon(draw: ImageDraw.ImageDraw, points: Iterable[tuple[float, float]],
            fill: tuple[int, int, int, int]) -> None:
    draw.polygon([sp(point) for point in points], fill=fill)


def human_frame(spec: dict, action: str, direction_index: int, frame: int) -> Image.Image:
    image = Image.new("RGBA", (CELL_W * SS, CELL_H * SS), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    fx, fy = DIRECTIONS[direction_index]
    side = abs(fx)
    front = fy > 0.3
    back = fy < -0.3
    phase = math.tau * frame / FRAMES

    if action == "idle":
        stride_amp, lift_amp = 0.0, 0.0
        bounce = math.sin(phase) * 0.55
        lean = 0.0
        arm_amp = 0.0
    elif action == "walk":
        stride_amp, lift_amp = 8.5, 5.0
        bounce = (1.0 - math.cos(phase * 2.0)) * 0.7
        lean = 0.5
        arm_amp = 7.0
    else:
        stride_amp, lift_amp = 14.5, 8.0
        bounce = abs(math.sin(phase)) * 2.4
        lean = 2.4
        arm_amp = 12.0

    cycle_l = math.sin(phase)
    cycle_r = -cycle_l
    lift_l = max(0.0, math.cos(phase)) if action != "idle" else 0.0
    lift_r = max(0.0, -math.cos(phase)) if action != "idle" else 0.0
    cx = CELL_W * 0.5 + fx * lean
    shoulder_y = 52.0 - bounce
    hip_y = 77.5 - bounce
    ground_y = 113.0
    shoulder_sep = 11.3 * (1.0 - 0.38 * side)
    hip_sep = 6.9 * (1.0 - 0.55 * side)
    outline = (17, 21, 26)

    # Back garments are separate silhouettes so coats and capes move with the gait.
    variant = spec["variant"]
    if variant == 2:
        sway = math.sin(phase) * (1.7 if action != "idle" else 0.4)
        polygon(draw, ((cx - 12, shoulder_y + 5), (cx + 11, shoulder_y + 5),
                       (cx + 13 + sway, 101), (cx + 3, 94),
                       (cx - 7 + sway * 0.3, 104), (cx - 13, 77)), rgba(shade(spec["accent"], 0.72)[:3]))
    elif variant == 4:
        sway = math.sin(phase) * (2.5 if action != "idle" else 0.7)
        polygon(draw, ((cx - 12, shoulder_y + 1), (cx + 11, shoulder_y + 2),
                       (cx + 18 + sway, 101), (cx + 3, 94),
                       (cx - 13 + sway * 0.35, 107), (cx - 16, 64)), rgba(shade(spec["secondary"], 0.72)[:3]))

    def leg_pose(side_sign: float, cycle: float, lift_value: float):
        hip = (cx + side_sign * hip_sep, hip_y)
        foot = (
            cx + side_sign * hip_sep * 1.08 + fx * cycle * stride_amp,
            ground_y + fy * cycle * (2.2 + side * 1.0) - lift_value * lift_amp,
        )
        knee = (
            (hip[0] + foot[0]) * 0.5 - fx * lift_value * 2.4 + side_sign * (1.1 - side),
            (hip[1] + foot[1]) * 0.5 - 1.8 - lift_value * 2.2,
        )
        return hip, knee, foot

    legs = [
        (-1.0, *leg_pose(-1.0, cycle_l, lift_l), cycle_l, lift_l),
        (1.0, *leg_pose(1.0, cycle_r, lift_r), cycle_r, lift_r),
    ]
    # The raised/rear leg is painted first; this makes contact frames read cleanly.
    legs.sort(key=lambda item: (item[5], item[3][1]))
    for side_sign, hip, knee, foot, cycle, lift_value in legs:
        limb(draw, (hip, knee, foot), spec["pants"], 7.0, outline)
        shoe_length = 7.5 if action != "run" else 8.5
        shoe_end = (foot[0] + (fx if abs(fx) > 0.2 else side_sign * 0.48) * shoe_length,
                    foot[1] + 0.7)
        rounded_line(draw, (foot, shoe_end), rgba(outline), 7.2)
        rounded_line(draw, (foot, shoe_end), rgba(spec["shoes"]), 4.9)

    # Far arm before torso, near arm after torso.
    arm_data = []
    for side_sign, leg_cycle in ((-1.0, cycle_l), (1.0, cycle_r)):
        arm_cycle = -leg_cycle
        shoulder = (cx + side_sign * shoulder_sep, shoulder_y + 1.0)
        hand = (
            cx + side_sign * (shoulder_sep + 2.8 * (1.0 - side)) + fx * arm_cycle * arm_amp,
            shoulder_y + 27.0 + fy * arm_cycle * 2.0 - (2.0 if action == "run" else 0.0),
        )
        elbow = ((shoulder[0] + hand[0]) * 0.5 - fx * arm_cycle * 1.8,
                 (shoulder[1] + hand[1]) * 0.5 + 1.5)
        arm_data.append((side_sign, shoulder, elbow, hand, arm_cycle))
    arm_data.sort(key=lambda item: item[3][1])
    far_arm, near_arm = arm_data[0], arm_data[1]

    limb(draw, far_arm[1:4], spec["dark"], 5.6, outline)
    joint(draw, far_arm[3], 3.1, spec["skin"], outline)

    # Tapered torso with clear shoulders and a stable vertical centerline.
    torso = ((cx - shoulder_sep - 2.2, shoulder_y),
             (cx + shoulder_sep + 2.2, shoulder_y),
             (cx + 8.4, hip_y + 4.2),
             (cx - 8.4, hip_y + 4.2))
    polygon(draw, torso, rgba(outline))
    inner = ((cx - shoulder_sep, shoulder_y + 2.0),
             (cx + shoulder_sep, shoulder_y + 2.0),
             (cx + 7.1, hip_y + 2.0),
             (cx - 7.1, hip_y + 2.0))
    polygon(draw, inner, rgba(spec["primary"]))

    # Clothing identity details stay large enough to survive in-world downscaling.
    if variant == 0:
        rounded_line(draw, ((cx - 7.5, shoulder_y + 3), (cx + 6.5, hip_y + 1)), rgba(spec["accent"]), 3.2)
        draw.rounded_rectangle(sbox((cx + 4, shoulder_y + 11, cx + 10, shoulder_y + 20)), radius=2 * SS, fill=rgba(spec["accent"]))
    elif variant == 1:
        draw.rounded_rectangle(sbox((cx - 7.5, shoulder_y + 7, cx + 7.5, shoulder_y + 21)), radius=3 * SS, fill=rgba(spec["secondary"]))
        draw.rounded_rectangle(sbox((cx + 5, hip_y - 5, cx + 10, hip_y + 1)), radius=1 * SS, fill=rgba(spec["accent"]))
    elif variant == 2:
        draw.rounded_rectangle(sbox((cx - 6.5, shoulder_y + 5, cx + 6.5, shoulder_y + 23)), radius=2 * SS, fill=rgba(spec["secondary"]))
        rounded_line(draw, ((cx + 3, shoulder_y + 4), (cx - 7, hip_y + 2)), rgba(spec["dark"]), 2.2)
    elif variant == 3:
        draw.rounded_rectangle(sbox((cx - 7, shoulder_y + 5, cx + 7, shoulder_y + 22)), radius=4 * SS, fill=rgba(spec["secondary"]))
        draw.rounded_rectangle(sbox((cx - 4, hip_y - 5, cx + 4, hip_y)), radius=1 * SS, fill=rgba(spec["accent"]))
    elif variant == 4:
        polygon(draw, ((cx - 7, shoulder_y + 3), (cx, shoulder_y + 13),
                       (cx + 7, shoulder_y + 3), (cx + 4, hip_y), (cx - 4, hip_y)), rgba(spec["secondary"]))
        draw.ellipse(sbox((cx - 3, shoulder_y + 10, cx + 3, shoulder_y + 16)), fill=rgba(spec["accent"]))
    elif variant == 10:
        polygon(draw, ((cx - 4, shoulder_y + 5), (cx, shoulder_y + 10),
                       (cx + 4, shoulder_y + 5), (cx, shoulder_y + 16)), fill=rgba(spec["accent"]))
        draw.rounded_rectangle(sbox((cx - 8, hip_y - 2, cx + 8, hip_y + 3)), radius=1 * SS, fill=rgba(spec["dark"]))
    elif variant == 11:
        draw.rounded_rectangle(sbox((cx - 7, shoulder_y + 6, cx + 7, shoulder_y + 16)), radius=2 * SS, fill=rgba(spec["secondary"]))
        draw.rounded_rectangle(sbox((cx + 3, shoulder_y + 8, cx + 8, shoulder_y + 13)), radius=1 * SS, fill=rgba(spec["accent"]))

    limb(draw, near_arm[1:4], spec["dark"], 5.6, outline)
    joint(draw, near_arm[3], 3.1, spec["skin"], outline)

    # Staff clipboard follows the leading hand instead of floating beside the body.
    if variant == 11:
        hx, hy = near_arm[3]
        draw.rounded_rectangle(sbox((hx - 4, hy - 5, hx + 4, hy + 6)), radius=1 * SS,
                               fill=rgba((210, 192, 150)))
        draw.rectangle(sbox((hx - 2, hy - 5.5, hx + 2, hy - 3.5)), fill=rgba((78, 64, 46)))

    # Neck and head. Side/diagonal views narrow the skull without rotating the full sprite.
    neck_x = cx + fx * (1.5 if side > 0.3 else 0.0)
    draw.rounded_rectangle(sbox((neck_x - 3.2, shoulder_y - 5, neck_x + 3.2, shoulder_y + 3)),
                           radius=2 * SS, fill=rgba(spec["skin"]))
    head_cx = cx + fx * (2.2 + side * 0.8)
    head_cy = 34.5 - bounce - (1.0 if action == "run" else 0.0)
    head_w = 18.5 - side * 3.0
    head_h = 23.0
    draw.ellipse(sbox((head_cx - head_w / 2 - 1.3, head_cy - head_h / 2 - 1.3,
                       head_cx + head_w / 2 + 1.3, head_cy + head_h / 2 + 1.3)), fill=rgba(outline))
    draw.ellipse(sbox((head_cx - head_w / 2, head_cy - head_h / 2,
                       head_cx + head_w / 2, head_cy + head_h / 2)), fill=rgba(spec["skin"]))

    hair = spec["hair"]
    if back:
        draw.pieslice(sbox((head_cx - head_w / 2 - 0.5, head_cy - head_h / 2 - 1,
                            head_cx + head_w / 2 + 0.5, head_cy + head_h / 2 + 2)), 176, 364, fill=rgba(hair))
        draw.rounded_rectangle(sbox((head_cx - head_w / 2, head_cy - 2,
                                     head_cx + head_w / 2, head_cy + 8)), radius=4 * SS, fill=rgba(hair))
    else:
        draw.pieslice(sbox((head_cx - head_w / 2 - 0.5, head_cy - head_h / 2 - 1,
                            head_cx + head_w / 2 + 0.5, head_cy + head_h / 2)), 180, 360, fill=rgba(hair))
        fringe = ((head_cx - head_w * 0.45, head_cy - 4),
                  (head_cx - head_w * 0.20, head_cy + 1),
                  (head_cx, head_cy - 5),
                  (head_cx + head_w * 0.18, head_cy + 1),
                  (head_cx + head_w * 0.43, head_cy - 4),
                  (head_cx + head_w * 0.35, head_cy - 9),
                  (head_cx - head_w * 0.35, head_cy - 9))
        polygon(draw, fringe, rgba(hair))

    if not back:
        eye_y = head_cy + 0.5
        if side > 0.82:
            eye_x = head_cx + fx * 3.0
            draw.ellipse(sbox((eye_x - 0.9, eye_y - 0.9, eye_x + 0.9, eye_y + 0.9)), fill=rgba((28, 31, 34)))
            polygon(draw, ((head_cx + fx * (head_w / 2 - 0.8), head_cy + 1),
                           (head_cx + fx * (head_w / 2 + 2.1), head_cy + 2.4),
                           (head_cx + fx * (head_w / 2 - 0.7), head_cy + 3.1)), rgba(spec["skin"]))
        else:
            eye_sep = 3.5 + side
            draw.ellipse(sbox((head_cx - eye_sep - 0.8, eye_y - 0.8,
                               head_cx - eye_sep + 0.8, eye_y + 0.8)), fill=rgba((27, 31, 35)))
            draw.ellipse(sbox((head_cx + eye_sep - 0.8, eye_y - 0.8,
                               head_cx + eye_sep + 0.8, eye_y + 0.8)), fill=rgba((27, 31, 35)))

    if variant == 1 and not back:
        draw.rounded_rectangle(sbox((head_cx - 7, head_cy - 2, head_cx + 7, head_cy + 3)),
                               radius=2 * SS, outline=rgba(spec["secondary"]), width=2 * SS)
    elif variant == 3:
        draw.ellipse(sbox((head_cx - head_w / 2 - 3, head_cy - 3,
                           head_cx - head_w / 2 + 1.5, head_cy + 4)), fill=rgba(spec["accent"]))
        draw.ellipse(sbox((head_cx + head_w / 2 - 1.5, head_cy - 3,
                           head_cx + head_w / 2 + 3, head_cy + 4)), fill=rgba(spec["accent"]))
    elif variant == 4:
        # Long side hair remains symmetrical in every frame.
        draw.rounded_rectangle(sbox((head_cx - head_w / 2 - 1, head_cy + 3,
                                     head_cx - head_w / 2 + 3, shoulder_y + 10)), radius=2 * SS, fill=rgba(hair))
        draw.rounded_rectangle(sbox((head_cx + head_w / 2 - 3, head_cy + 3,
                                     head_cx + head_w / 2 + 1, shoulder_y + 10)), radius=2 * SS, fill=rgba(hair))
    elif variant == 10:
        draw.rounded_rectangle(sbox((head_cx - 10, head_cy - 11, head_cx + 10, head_cy - 6)),
                               radius=2 * SS, fill=rgba(spec["dark"]))
        draw.rectangle(sbox((head_cx - 4, head_cy - 7, head_cx + fx * 8 + 4, head_cy - 5)), fill=rgba(spec["primary"]))

    return image.resize((CELL_W, CELL_H), Image.Resampling.LANCZOS)


def robot_frame(spec: dict, action: str, direction_index: int, frame: int) -> Image.Image:
    image = Image.new("RGBA", (CELL_W * SS, CELL_H * SS), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    fx, fy = DIRECTIONS[direction_index]
    side = abs(fx)
    phase = math.tau * frame / FRAMES
    if action == "idle":
        stride_amp, lift_amp, bounce = 0.0, 0.0, math.sin(phase) * 0.35
    elif action == "walk":
        stride_amp, lift_amp, bounce = 7.0, 3.5, abs(math.sin(phase)) * 0.8
    else:
        stride_amp, lift_amp, bounce = 11.5, 5.5, abs(math.sin(phase)) * 1.5
    cx = 48 + fx * (1.4 if action == "run" else 0.0)
    hip_y = 77 - bounce
    ground_y = 112
    cycle_l = math.sin(phase)
    cycle_r = -cycle_l
    lift_l = max(0.0, math.cos(phase)) if action != "idle" else 0.0
    lift_r = max(0.0, -math.cos(phase)) if action != "idle" else 0.0
    shell = spec["primary"]
    dark = spec["dark"]
    signal = spec["accent"]
    outline = (13, 18, 21)

    for side_sign, cycle, lift_value in ((-1, cycle_l, lift_l), (1, cycle_r, lift_r)):
        hip = (cx + side_sign * (5.5 - 2.8 * side), hip_y)
        foot = (cx + side_sign * 5 + fx * cycle * stride_amp,
                ground_y + fy * cycle * 2 - lift_value * lift_amp)
        knee = ((hip[0] + foot[0]) * 0.5 - fx * lift_value * 2,
                (hip[1] + foot[1]) * 0.5 - 2.5)
        limb(draw, (hip, knee, foot), dark, 5.5, outline)
        joint(draw, knee, 3.3, shell, outline)
        shoe_end = (foot[0] + (fx if abs(fx) > .2 else side_sign * .55) * 8, foot[1] + .5)
        rounded_line(draw, (foot, shoe_end), rgba(outline), 8)
        rounded_line(draw, (foot, shoe_end), rgba(shell), 5.5)

    body = (cx - 14 + side * 3, 51 - bounce, cx + 14 - side * 3, 82 - bounce)
    draw.rounded_rectangle(sbox(body), radius=7 * SS, fill=rgba(outline))
    draw.rounded_rectangle(sbox((body[0] + 2, body[1] + 2, body[2] - 2, body[3] - 2)),
                           radius=6 * SS, fill=rgba(shell))
    draw.rounded_rectangle(sbox((cx - 8, 59 - bounce, cx + 8, 72 - bounce)), radius=3 * SS, fill=rgba(dark))
    draw.ellipse(sbox((cx - 3.5, 62 - bounce, cx + 3.5, 69 - bounce)), fill=rgba(signal))

    arm_amp = 0 if action == "idle" else (6 if action == "walk" else 9)
    for side_sign, cycle in ((-1, cycle_l), (1, cycle_r)):
        shoulder = (cx + side_sign * (14 - side * 4), 58 - bounce)
        hand = (shoulder[0] - fx * cycle * arm_amp, 78 - bounce + fy * cycle * 1.5)
        elbow = ((shoulder[0] + hand[0]) / 2, (shoulder[1] + hand[1]) / 2)
        limb(draw, (shoulder, elbow, hand), dark, 4.5, outline)
        joint(draw, hand, 3.0, shell, outline)

    head_cx = cx + fx * 2.5
    head_y = 38 - bounce
    head_w = 22 - side * 5
    draw.rounded_rectangle(sbox((head_cx - head_w / 2 - 1.3, head_y - 12,
                                 head_cx + head_w / 2 + 1.3, head_y + 10)), radius=6 * SS, fill=rgba(outline))
    draw.rounded_rectangle(sbox((head_cx - head_w / 2, head_y - 10.5,
                                 head_cx + head_w / 2, head_y + 8.5)), radius=5 * SS, fill=rgba(shell))
    visor_x = head_cx + fx * (2.5 if side > .3 else 0)
    draw.rounded_rectangle(sbox((visor_x - head_w * .32, head_y - 3,
                                 visor_x + head_w * .32, head_y + 2.5)), radius=2 * SS, fill=rgba(dark))
    draw.rounded_rectangle(sbox((visor_x - head_w * .20, head_y - 1.8,
                                 visor_x + head_w * .20, head_y + 1.3)), radius=1 * SS, fill=rgba(signal))
    rounded_line(draw, ((head_cx, head_y - 11), (head_cx, head_y - 18)), rgba(shell), 2.3)
    joint(draw, (head_cx, head_y - 20), 2.4, signal, outline)
    return image.resize((CELL_W, CELL_H), Image.Resampling.LANCZOS)


def render_actor(spec: dict) -> Image.Image:
    sheet = Image.new("RGBA", (CELL_W * FRAMES, CELL_H * len(DIRECTIONS) * len(ACTIONS)), (0, 0, 0, 0))
    for action_index, action in enumerate(ACTIONS):
        for direction_index in range(len(DIRECTIONS)):
            row = action_index * len(DIRECTIONS) + direction_index
            for frame in range(FRAMES):
                if spec["kind"] == "robot":
                    cell = robot_frame(spec, action, direction_index, frame)
                else:
                    cell = human_frame(spec, action, direction_index, frame)
                sheet.alpha_composite(cell, (frame * CELL_W, row * CELL_H))
    return sheet


def save_preview(atlases: dict[str, Image.Image], destination: Path) -> None:
    names = list(atlases)
    tile_w, tile_h = CELL_W * 8, CELL_H * 3
    preview = Image.new("RGB", (tile_w, tile_h * len(names)), (31, 39, 45))
    draw = ImageDraw.Draw(preview)
    for actor_index, name in enumerate(names):
        atlas = atlases[name]
        top = actor_index * tile_h
        for action_index in range(3):
            # Down/right diagonal direction gives a readable three-quarter preview.
            row = action_index * 8 + 1
            for frame in range(8):
                cell = atlas.crop((frame * CELL_W, row * CELL_H,
                                   (frame + 1) * CELL_W, (row + 1) * CELL_H))
                x = frame * CELL_W
                y = top + action_index * CELL_H
                checker = (42, 51, 58) if (frame + action_index) % 2 == 0 else (48, 58, 65)
                preview.paste(checker, (x, y, x + CELL_W, y + CELL_H))
                preview.paste(cell.convert("RGB"), (x, y), cell.getchannel("A"))
        draw.text((6, top + 5), name, fill=(232, 241, 242))
    destination.parent.mkdir(parents=True, exist_ok=True)
    preview.save(destination, optimize=True)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output-dir", type=Path, required=True)
    parser.add_argument("--preview", type=Path)
    args = parser.parse_args()
    args.output_dir.mkdir(parents=True, exist_ok=True)
    atlases = {}
    for name, spec in ACTORS.items():
        atlas = render_actor(spec)
        atlas.save(args.output_dir / f"actor_{name}_sprites.png", optimize=True)
        atlases[name] = atlas
    if args.preview:
        save_preview(atlases, args.preview)
    print(f"Generated {len(atlases)} atlases at {args.output_dir}")


if __name__ == "__main__":
    main()
