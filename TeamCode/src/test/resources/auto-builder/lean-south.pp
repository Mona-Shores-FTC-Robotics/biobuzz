{
  "startPoint": {
    "x": 59,
    "y": 9.5,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [],
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
  "sequence": [],
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
    "exportName": "lean-south",
    "registry": {
      "actions": [
        "LaunchOne",
        "LaunchAll"
      ],
      "conditions": [
        "LeftCellUp",
        "IntakeFull",
        "RightCellUp",
        "Empty"
      ],
      "typicalS": {
        "LaunchOne": 0.5,
        "LaunchAll": 2.0
      },
      "events": []
    },
    "points": {
      "START": [
        59,
        9.5,
        90
      ],
      "HOME_S": [
        59,
        10,
        90
      ],
      "PARK_S": [
        14,
        93,
        330
      ],
      "PARK_S2": [
        14,
        95,
        330
      ]
    },
    "pathEnds": {},
    "startAt": "START",
    "cards": [
      {
        "id": "a-1",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "a-2",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "a-3",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "w-4",
        "kind": "firstOf",
        "label": "TIP 1",
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
        ]
      },
      {
        "id": "w-5",
        "kind": "firstOf",
        "label": "Catch the spill",
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
        "id": "w-6",
        "kind": "firstOf",
        "label": "South CELL up (1)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "label": "South CELL up (1) (2)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "label": "South CELL up (1) (3)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "label": "South CELL up (1) (4)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-10",
        "kind": "firstOf",
        "label": "South CELL up (1) (5)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-11",
        "kind": "firstOf",
        "label": "South CELL up (1) (6)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-12",
        "kind": "firstOf",
        "label": "South CELL up (1) (7)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-13",
        "kind": "firstOf",
        "label": "South CELL up (1) (8)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-14",
        "kind": "firstOf",
        "label": "Fire (1)",
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
        "id": "w-15",
        "kind": "firstOf",
        "label": "Until it tips (1)",
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
        ]
      },
      {
        "id": "w-16",
        "kind": "firstOf",
        "label": "Catch the spill (1)",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-17",
        "kind": "firstOf",
        "label": "South CELL up (2)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-18",
        "kind": "firstOf",
        "label": "South CELL up (2) (2)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-19",
        "kind": "firstOf",
        "label": "South CELL up (2) (3)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-20",
        "kind": "firstOf",
        "label": "South CELL up (2) (4)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-21",
        "kind": "firstOf",
        "label": "South CELL up (2) (5)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-22",
        "kind": "firstOf",
        "label": "South CELL up (2) (6)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "label": "South CELL up (2) (7)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "label": "South CELL up (2) (8)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "label": "Fire (2)",
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
        "id": "w-26",
        "kind": "firstOf",
        "label": "Until it tips (2)",
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
        ]
      },
      {
        "id": "w-27",
        "kind": "firstOf",
        "label": "Catch the spill (2)",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-28",
        "kind": "firstOf",
        "label": "South CELL up (3)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "label": "South CELL up (3) (2)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-30",
        "kind": "firstOf",
        "label": "South CELL up (3) (3)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-31",
        "kind": "firstOf",
        "label": "South CELL up (3) (4)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-32",
        "kind": "firstOf",
        "label": "South CELL up (3) (5)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "label": "South CELL up (3) (6)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "label": "South CELL up (3) (7)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "label": "South CELL up (3) (8)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "label": "Fire (3)",
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
        "id": "w-37",
        "kind": "firstOf",
        "label": "Until it tips (3)",
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
        ]
      },
      {
        "id": "w-38",
        "kind": "firstOf",
        "label": "Catch the spill (3)",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-39",
        "kind": "firstOf",
        "label": "South CELL up (4)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-40",
        "kind": "firstOf",
        "label": "South CELL up (4) (2)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-41",
        "kind": "firstOf",
        "label": "South CELL up (4) (3)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-42",
        "kind": "firstOf",
        "label": "South CELL up (4) (4)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-43",
        "kind": "firstOf",
        "label": "South CELL up (4) (5)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-44",
        "kind": "firstOf",
        "label": "South CELL up (4) (6)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-45",
        "kind": "firstOf",
        "label": "South CELL up (4) (7)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-46",
        "kind": "firstOf",
        "label": "South CELL up (4) (8)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-47",
        "kind": "firstOf",
        "label": "Fire (4)",
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
        "id": "w-48",
        "kind": "firstOf",
        "label": "Until it tips (4)",
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
        ]
      },
      {
        "id": "w-49",
        "kind": "firstOf",
        "label": "Catch the spill (4)",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ]
      },
      {
        "id": "w-50",
        "kind": "firstOf",
        "label": "South CELL up (5)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-51",
        "kind": "firstOf",
        "label": "South CELL up (5) (2)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-52",
        "kind": "firstOf",
        "label": "South CELL up (5) (3)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-53",
        "kind": "firstOf",
        "label": "South CELL up (5) (4)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-54",
        "kind": "firstOf",
        "label": "South CELL up (5) (5)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-55",
        "kind": "firstOf",
        "label": "South CELL up (5) (6)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-56",
        "kind": "firstOf",
        "label": "South CELL up (5) (7)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-57",
        "kind": "firstOf",
        "label": "South CELL up (5) (8)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-58",
        "kind": "firstOf",
        "label": "Fire (5)",
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
        "id": "w-59",
        "kind": "firstOf",
        "label": "Until it tips (5)",
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
        ]
      },
      {
        "id": "w-60",
        "kind": "firstOf",
        "label": "Catch the spill (5)",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 2000,
            "cards": []
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}