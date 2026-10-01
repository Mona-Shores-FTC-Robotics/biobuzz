{
  "startPoint": {
    "x": 59,
    "y": 132.25,
    "name": "START_N",
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-flower-n-1",
      "color": "#3cc8e4",
      "name": "START_N to FLOWER_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.4,
        "y": 130.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 90
      }
    },
    {
      "id": "to-start-n-2",
      "color": "#3cc8e4",
      "name": "FLOWER_N to START_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 132.25
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    },
    {
      "id": "to-slide-nl-3",
      "color": "#3cc8e4",
      "name": "START_N to SLIDE_NL",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41,
        "y": 132.25
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-slide-nr-4",
      "color": "#3cc8e4",
      "name": "SLIDE_NL to SLIDE_NR",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61,
        "y": 132.25
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-start-n-5",
      "color": "#3cc8e4",
      "name": "SLIDE_NR to START_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 132.25
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-slide-nl-6",
      "color": "#3cc8e4",
      "name": "START_N to SLIDE_NL",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41,
        "y": 132.25
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-slide-nr-7",
      "color": "#3cc8e4",
      "name": "SLIDE_NL to SLIDE_NR",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61,
        "y": 132.25
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-park-b-8",
      "color": "#3cc8e4",
      "name": "SLIDE_NR to PARK_B",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 15,
        "y": 118
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 90
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
      "lineId": "to-flower-n-1"
    },
    {
      "kind": "path",
      "lineId": "to-start-n-2"
    },
    {
      "kind": "path",
      "lineId": "to-slide-nl-3"
    },
    {
      "kind": "path",
      "lineId": "to-slide-nr-4"
    },
    {
      "kind": "path",
      "lineId": "to-start-n-5"
    },
    {
      "kind": "path",
      "lineId": "to-slide-nl-6"
    },
    {
      "kind": "path",
      "lineId": "to-slide-nr-7"
    },
    {
      "kind": "path",
      "lineId": "to-park-b-8"
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
    "maxVelocity": 60,
    "maxAcceleration": 55,
    "maxDeceleration": 55,
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
    "exportName": "duo-north",
    "registry": {
      "actions": [
        "LaunchOne",
        "LaunchAll",
        "SpinUp"
      ],
      "conditions": [
        "Tip",
        "IntakeFull",
        "LeftCellUp",
        "RightCellUp",
        "Empty"
      ],
      "typicalS": {
        "LaunchOne": 0.5,
        "LaunchAll": 2.0,
        "SpinUp": 0.1
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START_N": [
        59,
        132.25,
        270
      ],
      "SLIDE_NL": [
        41,
        132.25,
        270
      ],
      "SLIDE_NR": [
        61,
        132.25,
        270
      ],
      "FLOWER_N": [
        47.4,
        130.5,
        90
      ],
      "PARK_B": [
        15,
        118,
        90
      ]
    },
    "pathEnds": {
      "to-flower-n-1": "FLOWER_N",
      "to-start-n-2": "START_N",
      "to-slide-nl-3": "SLIDE_NL",
      "to-slide-nr-4": "SLIDE_NR",
      "to-start-n-5": "START_N",
      "to-slide-nl-6": "SLIDE_NL",
      "to-slide-nr-7": "SLIDE_NR",
      "to-park-b-8": "PARK_B"
    },
    "startAt": "START_N",
    "cards": [
      {
        "id": "a-1",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "w-2",
        "kind": "firstOf",
        "label": "South tips",
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
        "id": "w-3",
        "kind": "firstOf",
        "label": "South tips (2)",
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
        "id": "w-4",
        "kind": "firstOf",
        "label": "South tips (3)",
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
        "label": "Fire the preloads",
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
        "id": "p-6",
        "kind": "path",
        "lineId": "to-flower-n-1",
        "park": false
      },
      {
        "id": "w-7",
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
        "id": "p-8",
        "kind": "path",
        "lineId": "to-start-n-2",
        "park": false
      },
      {
        "id": "w-9",
        "kind": "firstOf",
        "label": "Fire until it tips",
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
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-10",
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
        "id": "w-11",
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
        "id": "w-12",
        "kind": "firstOf",
        "label": "Our CELL up (1)",
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
        "id": "w-13",
        "kind": "firstOf",
        "label": "Our CELL up (1) (2)",
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
        "id": "w-14",
        "kind": "firstOf",
        "label": "Our CELL up (1) (3)",
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
        "id": "w-15",
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
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-16",
        "kind": "path",
        "lineId": "to-slide-nl-3",
        "park": false
      },
      {
        "id": "p-17",
        "kind": "path",
        "lineId": "to-slide-nr-4",
        "park": false
      },
      {
        "id": "w-18",
        "kind": "firstOf",
        "label": "Sweep (1)",
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
        "id": "w-19",
        "kind": "firstOf",
        "label": "Fire until it tips (1)",
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
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-20",
        "kind": "firstOf",
        "label": "Fire until it tips (1) (2)",
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
        "id": "p-21",
        "kind": "path",
        "lineId": "to-start-n-5",
        "park": false
      },
      {
        "id": "w-22",
        "kind": "firstOf",
        "label": "Spill rolls in (1)",
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
        "id": "w-23",
        "kind": "firstOf",
        "label": "Our CELL up (2)",
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
        "id": "w-24",
        "kind": "firstOf",
        "label": "Our CELL up (2) (2)",
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
        "id": "w-25",
        "kind": "firstOf",
        "label": "Our CELL up (2) (3)",
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
        "id": "w-26",
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
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-27",
        "kind": "path",
        "lineId": "to-slide-nl-6",
        "park": false
      },
      {
        "id": "p-28",
        "kind": "path",
        "lineId": "to-slide-nr-7",
        "park": false
      },
      {
        "id": "w-29",
        "kind": "firstOf",
        "label": "Sweep (2)",
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
        "id": "w-30",
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
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-31",
        "kind": "firstOf",
        "label": "Fire until it tips (2) (2)",
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
        "id": "p-32",
        "kind": "path",
        "lineId": "to-park-b-8",
        "park": true
      }
    ]
  },
  "version": "1.5.0"
}