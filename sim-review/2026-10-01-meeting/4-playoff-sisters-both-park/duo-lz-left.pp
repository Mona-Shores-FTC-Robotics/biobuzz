{
  "startPoint": {
    "x": 59,
    "y": 132.25,
    "name": "START",
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-flower-l-in-1",
      "color": "#3cc8e4",
      "name": "START to FLOWER_L_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 121.79
      },
      "controlPoints": [
        {
          "x": 59,
          "y": 121.79
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-flower-l-turn-2",
      "color": "#3cc8e4",
      "name": "FLOWER_L_IN to FLOWER_L_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 119.29
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 90
      }
    },
    {
      "id": "to-flower-l-3",
      "color": "#3cc8e4",
      "name": "FLOWER_L_TURN to FLOWER_L",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 127.59
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-flower-l-in-4",
      "color": "#3cc8e4",
      "name": "FLOWER_L to FLOWER_L_IN",
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
      "id": "to-flower-l-turn-5",
      "color": "#3cc8e4",
      "name": "FLOWER_L_IN to FLOWER_L_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 119.29
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-flower-l-back-home-l-6",
      "color": "#3cc8e4",
      "name": "FLOWER_L_TURN to FLOWER_L_BACK_HOME_L",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 119.29
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    },
    {
      "id": "to-home-l-7",
      "color": "#3cc8e4",
      "name": "FLOWER_L_BACK_HOME_L to HOME_L",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-park-l-8",
      "color": "#3cc8e4",
      "name": "HOME_L to PARK_L",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 15,
        "y": 126
      },
      "controlPoints": [
        {
          "x": 59,
          "y": 118
        },
        {
          "x": 30,
          "y": 118
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 300
      }
    },
    {
      "id": "to-park-n2-9",
      "color": "#3cc8e4",
      "name": "PARK_L to PARK_N2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 15,
        "y": 124
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 300,
        "endDeg": 300
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
      "lineId": "to-flower-l-in-1"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-turn-2"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-3"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-in-4"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-turn-5"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-back-home-l-6"
    },
    {
      "kind": "path",
      "lineId": "to-home-l-7"
    },
    {
      "kind": "path",
      "lineId": "to-park-l-8"
    },
    {
      "kind": "path",
      "lineId": "to-park-n2-9"
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
    "exportName": "duo-lz-left",
    "registry": {
      "actions": [
        "SpinUp",
        "LaunchAll"
      ],
      "conditions": [
        "LeftCellUp",
        "Empty",
        "IntakeFull",
        "RightCellUp"
      ],
      "typicalS": {
        "SpinUp": 0.1,
        "LaunchAll": 2.0
      },
      "events": []
    },
    "points": {
      "START": [
        59,
        132.25,
        270
      ],
      "FLOWER_L": [
        47.36,
        127.59,
        90
      ],
      "FLOWER_L_IN": [
        47.36,
        121.79,
        90
      ],
      "FLOWER_L_TURN": [
        47.36,
        119.29,
        90
      ],
      "HOME_L": [
        59,
        131.75,
        270
      ],
      "PARK_L": [
        15,
        126,
        300
      ],
      "PARK_N2": [
        15,
        124,
        300
      ],
      "FLOWER_L_BACK_HOME_L": [
        59,
        119.29,
        270
      ]
    },
    "pathEnds": {
      "to-flower-l-in-1": "FLOWER_L_IN",
      "to-flower-l-turn-2": "FLOWER_L_TURN",
      "to-flower-l-3": "FLOWER_L",
      "to-flower-l-in-4": "FLOWER_L_IN",
      "to-flower-l-turn-5": "FLOWER_L_TURN",
      "to-flower-l-back-home-l-6": "FLOWER_L_BACK_HOME_L",
      "to-home-l-7": "HOME_L",
      "to-park-l-8": "PARK_L",
      "to-park-n2-9": "PARK_N2"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "a-1",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "w-2",
        "kind": "firstOf",
        "label": "Right tips",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-3",
        "kind": "firstOf",
        "label": "Right tips (2)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-4",
        "kind": "firstOf",
        "label": "Right tips (3)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-5",
        "kind": "firstOf",
        "label": "Right tips (4)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-6",
        "kind": "firstOf",
        "label": "Right tips (5)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-7",
        "kind": "firstOf",
        "label": "Right tips (6)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-8",
        "kind": "firstOf",
        "label": "Right tips (7)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-9",
        "kind": "firstOf",
        "label": "Right tips (8)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 500,
            "cards": []
          }
        ]
      },
      {
        "id": "w-10",
        "kind": "firstOf",
        "label": "Fire the preloads",
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
        "id": "p-11",
        "kind": "path",
        "lineId": "to-flower-l-in-1",
        "park": false
      },
      {
        "id": "p-12",
        "kind": "path",
        "lineId": "to-flower-l-turn-2",
        "park": false
      },
      {
        "id": "p-13",
        "kind": "path",
        "lineId": "to-flower-l-3",
        "park": false
      },
      {
        "id": "w-14",
        "kind": "firstOf",
        "label": "Collect at the FLOWER",
        "rows": [
          {
            "when": [
              "IntakeFull"
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
        "id": "p-15",
        "kind": "path",
        "lineId": "to-flower-l-in-4",
        "park": false
      },
      {
        "id": "p-16",
        "kind": "path",
        "lineId": "to-flower-l-turn-5",
        "park": false
      },
      {
        "id": "p-17",
        "kind": "path",
        "lineId": "to-flower-l-back-home-l-6",
        "park": false
      },
      {
        "id": "p-18",
        "kind": "path",
        "lineId": "to-home-l-7",
        "park": false
      },
      {
        "id": "w-19",
        "kind": "firstOf",
        "label": "Fire until it tips (TIP 2)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-20",
        "kind": "firstOf",
        "label": "Fire until it tips (2)",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 1500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-21",
        "kind": "firstOf",
        "label": "Spill rolls in",
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
        "id": "w-22",
        "kind": "firstOf",
        "label": "Our CELL up again",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-23",
        "kind": "firstOf",
        "label": "Our CELL up again (2)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-24",
        "kind": "firstOf",
        "label": "Our CELL up again (3)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-25",
        "kind": "firstOf",
        "label": "Our CELL up again (4)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-26",
        "kind": "firstOf",
        "label": "Our CELL up again (5)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-27",
        "kind": "firstOf",
        "label": "Our CELL up again (6)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-28",
        "kind": "firstOf",
        "label": "Our CELL up again (7)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-29",
        "kind": "firstOf",
        "label": "Our CELL up again (8)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 500,
            "cards": []
          }
        ]
      },
      {
        "id": "w-30",
        "kind": "firstOf",
        "label": "Fire (TIP 4)",
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
        "id": "p-31",
        "kind": "path",
        "lineId": "to-park-l-8",
        "park": false
      },
      {
        "id": "w-32",
        "kind": "firstOf",
        "label": "Left CELL up",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-33",
        "kind": "firstOf",
        "label": "Left CELL up (2)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-34",
        "kind": "firstOf",
        "label": "Left CELL up (3)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-35",
        "kind": "firstOf",
        "label": "Left CELL up (4)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-36",
        "kind": "firstOf",
        "label": "Left CELL up (5)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-37",
        "kind": "firstOf",
        "label": "Fire from the LOADING ZONE",
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
        "id": "p-38",
        "kind": "path",
        "lineId": "to-park-n2-9",
        "park": true
      }
    ]
  },
  "version": "1.5.0"
}