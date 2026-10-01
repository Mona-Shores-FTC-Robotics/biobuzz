{
  "startPoint": {
    "x": 59,
    "y": 9.5,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-garden-in-1",
      "color": "#3cc8e4",
      "name": "START to GARDEN_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 22
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 22
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.65,
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
      "id": "to-garden-2",
      "color": "#3cc8e4",
      "name": "GARDEN_IN to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 11
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      }
    },
    {
      "id": "to-slide-sl-3",
      "color": "#3cc8e4",
      "name": "GARDEN to SLIDE_SL",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41,
        "y": 9.5
      },
      "controlPoints": [
        {
          "x": 20,
          "y": 18
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-slide-sr-4",
      "color": "#3cc8e4",
      "name": "SLIDE_SL to SLIDE_SR",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 60,
        "y": 9.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-home-r-5",
      "color": "#3cc8e4",
      "name": "SLIDE_SR to HOME_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-park-r-6",
      "color": "#3cc8e4",
      "name": "HOME_R to PARK_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14,
        "y": 89
      },
      "controlPoints": [
        {
          "x": 24,
          "y": 14
        },
        {
          "x": 18,
          "y": 50
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 330
              }
            },
            {
              "startProgress": 0.65,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 330
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-r2-7",
      "color": "#3cc8e4",
      "name": "PARK_R to PARK_R2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14,
        "y": 90
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 330,
        "endDeg": 330
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
      "lineId": "to-garden-in-1"
    },
    {
      "kind": "path",
      "lineId": "to-garden-2"
    },
    {
      "kind": "path",
      "lineId": "to-slide-sl-3"
    },
    {
      "kind": "path",
      "lineId": "to-slide-sr-4"
    },
    {
      "kind": "path",
      "lineId": "to-home-r-5"
    },
    {
      "kind": "path",
      "lineId": "to-park-r-6"
    },
    {
      "kind": "path",
      "lineId": "to-park-r2-7"
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
    "exportName": "duo-lz-right",
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
      "SLIDE_SL": [
        41,
        9.5,
        90
      ],
      "SLIDE_SR": [
        60,
        9.5,
        90
      ],
      "HOME_R": [
        59,
        10,
        90
      ],
      "GARDEN_IN": [
        8.5,
        22,
        270
      ],
      "GARDEN": [
        8.5,
        11,
        270
      ],
      "PARK_R": [
        14,
        89,
        330
      ],
      "PARK_R2": [
        14,
        90,
        330
      ]
    },
    "pathEnds": {
      "to-garden-in-1": "GARDEN_IN",
      "to-garden-2": "GARDEN",
      "to-slide-sl-3": "SLIDE_SL",
      "to-slide-sr-4": "SLIDE_SR",
      "to-home-r-5": "HOME_R",
      "to-park-r-6": "PARK_R",
      "to-park-r2-7": "PARK_R2"
    },
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
        "label": "Tip 1",
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
        "label": "Spill rolls in",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 1800,
            "cards": []
          }
        ]
      },
      {
        "id": "w-6",
        "kind": "firstOf",
        "label": "Our CELL up",
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
        "label": "Our CELL up (2)",
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
        "label": "Our CELL up (3)",
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
        "label": "Our CELL up (4)",
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
        "label": "Our CELL up (5)",
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
        "label": "Our CELL up (6)",
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
        "label": "Our CELL up (7)",
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
        "label": "Our CELL up (8)",
        "rows": [
          {
            "when": [
              "RightCellUp"
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
        "id": "w-14",
        "kind": "firstOf",
        "label": "Fire",
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
        "id": "p-15",
        "kind": "path",
        "lineId": "to-garden-in-1",
        "park": false
      },
      {
        "id": "p-16",
        "kind": "path",
        "lineId": "to-garden-2",
        "park": false
      },
      {
        "id": "w-17",
        "kind": "firstOf",
        "label": "Collect in the GARDEN",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 1200,
            "cards": []
          }
        ]
      },
      {
        "id": "p-18",
        "kind": "path",
        "lineId": "to-slide-sl-3",
        "park": false
      },
      {
        "id": "p-19",
        "kind": "path",
        "lineId": "to-slide-sr-4",
        "park": false
      },
      {
        "id": "w-20",
        "kind": "firstOf",
        "label": "Sweep",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 300,
            "cards": []
          }
        ]
      },
      {
        "id": "w-21",
        "kind": "firstOf",
        "label": "Fire until it tips (TIP 3)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-22",
        "kind": "firstOf",
        "label": "Fire until it tips (2)",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "p-23",
        "kind": "path",
        "lineId": "to-home-r-5",
        "park": false
      },
      {
        "id": "w-24",
        "kind": "firstOf",
        "label": "Spill rolls in (2)",
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
        "id": "p-25",
        "kind": "path",
        "lineId": "to-park-r-6",
        "park": false
      },
      {
        "id": "w-26",
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
        "id": "w-27",
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
        "id": "w-28",
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
        "id": "w-29",
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
        "id": "w-30",
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
        "id": "w-31",
        "kind": "firstOf",
        "label": "Left CELL up (6)",
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
        "id": "w-32",
        "kind": "firstOf",
        "label": "Left CELL up (7)",
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
        "label": "Left CELL up (8)",
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
        "label": "Left CELL up (9)",
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
        "label": "Left CELL up (10)",
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
        "label": "Fire from the LOADING ZONE (TIP 4)",
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
        "id": "p-37",
        "kind": "path",
        "lineId": "to-park-r2-7",
        "park": true
      }
    ]
  },
  "version": "1.5.0"
}