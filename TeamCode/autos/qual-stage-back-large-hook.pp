{
  "startPoint": {
    "x": 59,
    "y": 132.25,
    "name": "START",
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-n-stage-1",
      "color": "#3cc8e4",
      "name": "START to N_STAGE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 120
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-n-clear-2",
      "color": "#3cc8e4",
      "name": "N_STAGE to N_CLEAR",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 127
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-n-clear-turn-3",
      "color": "#3cc8e4",
      "name": "N_CLEAR to N_CLEAR_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55.5,
        "y": 127
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 90
      }
    },
    {
      "id": "to-far-flower-in-4",
      "color": "#3cc8e4",
      "name": "N_CLEAR_TURN to FAR_FLOWER_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 121.79
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-far-flower-5",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER_IN to FAR_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 129.59
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-n-clear-turn-6",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to N_CLEAR_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55.5,
        "y": 127
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-n-clear-7",
      "color": "#3cc8e4",
      "name": "N_CLEAR_TURN to N_CLEAR",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 127
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    },
    {
      "id": "to-n-fire-8",
      "color": "#3cc8e4",
      "name": "N_CLEAR to N_FIRE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 119
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-n-pick-9",
      "color": "#3cc8e4",
      "name": "N_FIRE to N_PICK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 118
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-n-fire-10",
      "color": "#3cc8e4",
      "name": "N_PICK to N_FIRE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 119
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-n-hook-11",
      "color": "#3cc8e4",
      "name": "N_FIRE to N_HOOK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61.6,
        "y": 111.9
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-s-fire-12",
      "color": "#3cc8e4",
      "name": "N_HOOK to S_FIRE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 24
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.86,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.86,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-sweep-e-13",
      "color": "#3cc8e4",
      "name": "S_FIRE to SWEEP_E",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 180
      }
    },
    {
      "id": "to-sweep-w-14",
      "color": "#3cc8e4",
      "name": "SWEEP_E to SWEEP_W",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 22,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-garden-15",
      "color": "#3cc8e4",
      "name": "SWEEP_W to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 9.0
      },
      "controlPoints": [
        {
          "x": 8.5,
          "y": 14
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-s-fire-16",
      "color": "#3cc8e4",
      "name": "GARDEN to S_FIRE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 24
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-17",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13,
        "y": 88.0
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 24
        },
        {
          "x": 24,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-garden-18",
      "color": "#3cc8e4",
      "name": "S_FIRE to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 9.0
      },
      "controlPoints": [
        {
          "x": 8.5,
          "y": 30
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.7,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.7,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-19",
      "color": "#3cc8e4",
      "name": "GARDEN to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13,
        "y": 88.0
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 22
        },
        {
          "x": 26,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-s-fire-20",
      "color": "#3cc8e4",
      "name": "GARDEN to S_FIRE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 24
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-21",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13,
        "y": 88.0
      },
      "controlPoints": [
        {
          "x": 28,
          "y": 24
        },
        {
          "x": 24,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    }
  ],
  "shapes": [
    {
      "id": "frame-leg-red",
      "name": "HIVE frame leg (red side)",
      "vertices": [
        {
          "x": 46.6,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 51.2
        },
        {
          "x": 49.4,
          "y": 90.3
        },
        {
          "x": 46.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "frame-leg-blue",
      "name": "HIVE frame leg (blue side)",
      "vertices": [
        {
          "x": 92.6,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 51.2
        },
        {
          "x": 95,
          "y": 90.3
        },
        {
          "x": 92.6,
          "y": 90.3
        }
      ],
      "color": "#dc2626",
      "fillColor": "#ff6b6b"
    },
    {
      "id": "blue-half",
      "name": "Blue half: stay out",
      "vertices": [
        {
          "x": 70.8,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 0
        },
        {
          "x": 141.5,
          "y": 141.5
        },
        {
          "x": 70.8,
          "y": 141.5
        }
      ],
      "color": "#2563eb",
      "fillColor": "#60a5fa"
    }
  ],
  "sequence": [
    {
      "kind": "path",
      "lineId": "to-n-stage-1"
    },
    {
      "kind": "path",
      "lineId": "to-n-clear-2"
    },
    {
      "kind": "path",
      "lineId": "to-n-clear-turn-3"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-in-4"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-5"
    },
    {
      "kind": "path",
      "lineId": "to-n-clear-turn-6"
    },
    {
      "kind": "path",
      "lineId": "to-n-clear-7"
    },
    {
      "kind": "path",
      "lineId": "to-n-fire-8"
    },
    {
      "kind": "path",
      "lineId": "to-n-pick-9"
    },
    {
      "kind": "path",
      "lineId": "to-n-fire-10"
    },
    {
      "kind": "path",
      "lineId": "to-n-hook-11"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-12"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-e-13"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-w-14"
    },
    {
      "kind": "path",
      "lineId": "to-garden-15"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-16"
    },
    {
      "kind": "path",
      "lineId": "to-park-17"
    },
    {
      "kind": "path",
      "lineId": "to-garden-18"
    },
    {
      "kind": "path",
      "lineId": "to-park-19"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-20"
    },
    {
      "kind": "path",
      "lineId": "to-park-21"
    }
  ],
  "settings": {
    "xVelocity": 75,
    "yVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rWidth": 18,
    "rHeight": 18,
    "safetyMargin": 1,
    "maxVelocity": 50,
    "maxAcceleration": 45.0,
    "maxDeceleration": 45.0,
    "fieldMap": "biobuzz.webp",
    "robotImage": "/robot.png",
    "showGhostPaths": false,
    "showOnionLayers": false,
    "onionLayerSpacing": 3,
    "onionColor": "#dc2626",
    "onionNextPointOnly": false,
    "showHeadingArrow": false,
    "showCurrentTValue": false,
    "leftPanelWidth": 262,
    "rightPanelWidth": 714,
    "headingArrowLength": 50,
    "headingArrowColor": "#ffffff",
    "headingArrowThickness": 2,
    "pathOpacity": 1,
    "leftPanelMinWidth": 0,
    "rightPanelMinWidth": 0,
    "penToolMaxPaths": 8,
    "curveThroughMaxPoints": 4,
    "experimentalFeatures": {
      "optimize": false,
      "curveThrough": false
    }
  },
  "auto": {
    "version": 1,
    "drawnFor": "RED",
    "exportName": "qual-stage-back-large-hook",
    "registry": {
      "actions": [
        "SpinUp",
        "HookDown",
        "Outtake",
        "HookUp",
        "IntakeOn",
        "LaunchAll",
        "CollectSeen"
      ],
      "conditions": [
        "Empty",
        "IntakeFull",
        "LeftCellUp",
        "Tip",
        "RightCellUp"
      ],
      "typicalS": {
        "SpinUp": 0.1,
        "HookDown": 1.0,
        "Outtake": 1.0,
        "HookUp": 1.0,
        "IntakeOn": 0.1,
        "LaunchAll": 2.0,
        "CollectSeen": 2.0
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START": [
        59,
        132.25,
        270
      ],
      "S_SHOOT": [
        57.5,
        14,
        90
      ],
      "N_SHOOT": [
        57.5,
        127.5,
        270
      ],
      "N_LOW": [
        57.5,
        114,
        270
      ],
      "N_TURN": [
        57.5,
        104,
        270
      ],
      "S_CATCH": [
        57.5,
        28,
        90
      ],
      "N_CATCH": [
        57.5,
        113.5,
        270
      ],
      "S_BACK": [
        57.5,
        25,
        90
      ],
      "N_BACK": [
        57.5,
        116.5,
        270
      ],
      "GARDEN_IN": [
        8.5,
        22,
        270
      ],
      "GARDEN": [
        8.5,
        9.0,
        270
      ],
      "PARK": [
        13,
        88.0,
        90
      ],
      "FAR_FLOWER": [
        47.36,
        129.59,
        90
      ],
      "FAR_FLOWER_IN": [
        47.36,
        121.79,
        90
      ],
      "FAR_FLOWER_TURN": [
        47.36,
        119.29,
        90
      ],
      "S_FIRE": [
        57.5,
        24,
        90
      ],
      "N_FIRE": [
        57.5,
        119,
        270
      ],
      "N_STAGE": [
        57.5,
        120,
        270
      ],
      "N_PICK": [
        57.5,
        118,
        270
      ],
      "N_CLEAR": [
        57.5,
        127,
        270
      ],
      "N_CLEAR_TURN": [
        55.5,
        127,
        90
      ],
      "N_HOOK": [
        61.6,
        111.9,
        270
      ],
      "SWEEP_E": [
        57.5,
        10,
        180
      ],
      "SWEEP_W": [
        22,
        10,
        180
      ]
    },
    "pathEnds": {
      "to-n-stage-1": "N_STAGE",
      "to-n-clear-2": "N_CLEAR",
      "to-n-clear-turn-3": "N_CLEAR_TURN",
      "to-far-flower-in-4": "FAR_FLOWER_IN",
      "to-far-flower-5": "FAR_FLOWER",
      "to-n-clear-turn-6": "N_CLEAR_TURN",
      "to-n-clear-7": "N_CLEAR",
      "to-n-fire-8": "N_FIRE",
      "to-n-pick-9": "N_PICK",
      "to-n-fire-10": "N_FIRE",
      "to-n-hook-11": "N_HOOK",
      "to-s-fire-12": "S_FIRE",
      "to-sweep-e-13": "SWEEP_E",
      "to-sweep-w-14": "SWEEP_W",
      "to-garden-15": "GARDEN",
      "to-s-fire-16": "S_FIRE",
      "to-park-17": "PARK",
      "to-garden-18": "GARDEN",
      "to-park-19": "PARK",
      "to-s-fire-20": "S_FIRE",
      "to-park-21": "PARK"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "a-1",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "p-2",
        "kind": "path",
        "lineId": "to-n-stage-1",
        "park": false
      },
      {
        "id": "a-3",
        "kind": "action",
        "name": "HookDown"
      },
      {
        "id": "w-4",
        "kind": "firstOf",
        "label": "Stage the preloads in the hook",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 1500,
            "cards": []
          }
        ],
        "alongside": "Outtake"
      },
      {
        "id": "w-5",
        "kind": "firstOf",
        "label": "They stop rolling",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 400,
            "cards": []
          }
        ]
      },
      {
        "id": "a-6",
        "kind": "action",
        "name": "HookUp"
      },
      {
        "id": "p-7",
        "kind": "path",
        "lineId": "to-n-clear-2",
        "park": false
      },
      {
        "id": "p-8",
        "kind": "path",
        "lineId": "to-n-clear-turn-3",
        "park": false
      },
      {
        "id": "a-9",
        "kind": "action",
        "name": "IntakeOn"
      },
      {
        "id": "p-10",
        "kind": "path",
        "lineId": "to-far-flower-in-4",
        "park": false
      },
      {
        "id": "p-11",
        "kind": "path",
        "lineId": "to-far-flower-5",
        "park": false
      },
      {
        "id": "w-12",
        "kind": "firstOf",
        "label": "The far FLOWER",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2300,
            "cards": []
          }
        ]
      },
      {
        "id": "p-13",
        "kind": "path",
        "lineId": "to-n-clear-turn-6",
        "park": false
      },
      {
        "id": "p-14",
        "kind": "path",
        "lineId": "to-n-clear-7",
        "park": false
      },
      {
        "id": "p-15",
        "kind": "path",
        "lineId": "to-n-fire-8",
        "park": false
      },
      {
        "id": "w-16",
        "kind": "firstOf",
        "label": "TIP 1 (the partner)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 9000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-17",
        "kind": "firstOf",
        "label": "Fire the far FLOWER",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-18",
        "kind": "path",
        "lineId": "to-n-pick-9",
        "park": false
      },
      {
        "id": "w-19",
        "kind": "firstOf",
        "label": "Pick up the staged preloads",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 1500,
            "cards": []
          }
        ],
        "alongside": "CollectSeen"
      },
      {
        "id": "p-20",
        "kind": "path",
        "lineId": "to-n-fire-10",
        "park": false
      },
      {
        "id": "w-21",
        "kind": "firstOf",
        "label": "Fire the staged preloads (TIP 2)",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-22",
        "kind": "path",
        "lineId": "to-n-hook-11",
        "park": false
      },
      {
        "id": "w-23",
        "kind": "firstOf",
        "label": "TIP 2 settles",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 2500,
            "cards": []
          }
        ]
      },
      {
        "id": "w-24",
        "kind": "firstOf",
        "label": "It lands",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 1000,
            "cards": []
          }
        ]
      },
      {
        "id": "p-25",
        "kind": "path",
        "lineId": "to-s-fire-12",
        "park": false
      },
      {
        "id": "w-26",
        "kind": "firstOf",
        "label": "Fire TIP 2's spill",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-27",
        "kind": "path",
        "lineId": "to-sweep-e-13",
        "park": false
      },
      {
        "id": "p-28",
        "kind": "path",
        "lineId": "to-sweep-w-14",
        "park": false
      },
      {
        "id": "p-29",
        "kind": "path",
        "lineId": "to-garden-15",
        "park": false
      },
      {
        "id": "w-30",
        "kind": "firstOf",
        "label": "The GARDEN",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 1500,
            "cards": []
          }
        ]
      },
      {
        "id": "p-31",
        "kind": "path",
        "lineId": "to-s-fire-16",
        "park": false
      },
      {
        "id": "w-32",
        "kind": "firstOf",
        "label": "Fire the GARDEN",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 1800,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-41",
        "kind": "firstOf",
        "label": "No TIP 3 yet?",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [
              {
                "id": "p-34",
                "kind": "path",
                "lineId": "to-garden-18",
                "park": false
              },
              {
                "id": "w-40",
                "kind": "firstOf",
                "label": "Still no TIP 3?",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": [
                      {
                        "id": "w-36",
                        "kind": "firstOf",
                        "label": "The GARDEN again",
                        "rows": [
                          {
                            "when": [
                              "IntakeFull"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1100,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "p-37",
                        "kind": "path",
                        "lineId": "to-s-fire-20",
                        "park": false
                      },
                      {
                        "id": "w-38",
                        "kind": "firstOf",
                        "label": "Fire the GARDEN again",
                        "rows": [
                          {
                            "when": [
                              "LeftCellUp"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 2500,
                            "cards": []
                          }
                        ],
                        "alongside": "LaunchAll"
                      },
                      {
                        "id": "p-39",
                        "kind": "path",
                        "lineId": "to-park-21",
                        "park": false
                      }
                    ],
                    "label": "No TIP: fire the GARDEN"
                  },
                  {
                    "afterMs": 20,
                    "cards": [
                      {
                        "id": "p-35",
                        "kind": "path",
                        "lineId": "to-park-19",
                        "park": true
                      }
                    ],
                    "label": "TIP 3: PARK"
                  }
                ]
              }
            ],
            "label": "No TIP: the GARDEN"
          },
          {
            "afterMs": 20,
            "cards": [
              {
                "id": "p-33",
                "kind": "path",
                "lineId": "to-park-17",
                "park": true
              }
            ],
            "label": "TIP 3: PARK"
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}