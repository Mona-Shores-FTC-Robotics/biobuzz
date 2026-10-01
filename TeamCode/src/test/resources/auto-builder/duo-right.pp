{
  "startPoint": {
    "x": 59,
    "y": 9.5,
    "name": "START_R",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-slide-sl-1",
      "color": "#3cc8e4",
      "name": "START_R to SLIDE_SL",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41,
        "y": 9.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-slide-sr-2",
      "color": "#3cc8e4",
      "name": "SLIDE_SL to SLIDE_SR",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61,
        "y": 9.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-start-s-3",
      "color": "#3cc8e4",
      "name": "SLIDE_SR to START_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 9.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-slide-sl-4",
      "color": "#3cc8e4",
      "name": "START_R to SLIDE_SL",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41,
        "y": 9.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-slide-sr-5",
      "color": "#3cc8e4",
      "name": "SLIDE_SL to SLIDE_SR",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61,
        "y": 9.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-start-s-6",
      "color": "#3cc8e4",
      "name": "SLIDE_SR to START_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 9.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-slide-sl-7",
      "color": "#3cc8e4",
      "name": "START_R to SLIDE_SL",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 41,
        "y": 9.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-slide-sr-8",
      "color": "#3cc8e4",
      "name": "SLIDE_SL to SLIDE_SR",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 61,
        "y": 9.5
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-park-a-9",
      "color": "#3cc8e4",
      "name": "SLIDE_SR to PARK_A",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 15,
        "y": 99
      },
      "controlPoints": [
        {
          "x": 10,
          "y": 40
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
      "lineId": "to-slide-sl-1"
    },
    {
      "kind": "path",
      "lineId": "to-slide-sr-2"
    },
    {
      "kind": "path",
      "lineId": "to-start-s-3"
    },
    {
      "kind": "path",
      "lineId": "to-slide-sl-4"
    },
    {
      "kind": "path",
      "lineId": "to-slide-sr-5"
    },
    {
      "kind": "path",
      "lineId": "to-start-s-6"
    },
    {
      "kind": "path",
      "lineId": "to-slide-sl-7"
    },
    {
      "kind": "path",
      "lineId": "to-slide-sr-8"
    },
    {
      "kind": "path",
      "lineId": "to-park-a-9"
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
    "exportName": "duo-right",
    "registry": {
      "actions": [
        "LaunchOne",
        "LaunchAll",
        "SpinUp",
        "CollectSeen"
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
        "SpinUp": 0.1,
        "CollectSeen": 2.0
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START_R": [
        59,
        9.5,
        90
      ],
      "HOME_R": [
        59,
        10,
        90
      ],
      "SLIDE_SL": [
        41,
        9.5,
        90
      ],
      "SLIDE_SR": [
        61,
        9.5,
        90
      ],
      "PARK_A": [
        15,
        99,
        90
      ]
    },
    "pathEnds": {
      "to-slide-sl-1": "SLIDE_SL",
      "to-slide-sr-2": "SLIDE_SR",
      "to-start-s-3": "START_R",
      "to-slide-sl-4": "SLIDE_SL",
      "to-slide-sr-5": "SLIDE_SR",
      "to-start-s-6": "START_R",
      "to-slide-sl-7": "SLIDE_SL",
      "to-slide-sr-8": "SLIDE_SR",
      "to-park-a-9": "PARK_A"
    },
    "startAt": "START_R",
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
        "label": "Our CELL up (1)",
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
        "id": "w-7",
        "kind": "firstOf",
        "label": "Our CELL up (1) (2)",
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
        "id": "w-8",
        "kind": "firstOf",
        "label": "Our CELL up (1) (3)",
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
        "id": "w-9",
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
        "id": "p-10",
        "kind": "path",
        "lineId": "to-slide-sl-1",
        "park": false
      },
      {
        "id": "p-11",
        "kind": "path",
        "lineId": "to-slide-sr-2",
        "park": false
      },
      {
        "id": "w-12",
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
        "id": "w-13",
        "kind": "firstOf",
        "label": "Fire until it tips (1)",
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
        "id": "w-14",
        "kind": "firstOf",
        "label": "Fire until it tips (1) (2)",
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
        "id": "p-15",
        "kind": "path",
        "lineId": "to-start-s-3",
        "park": false
      },
      {
        "id": "w-16",
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
        "id": "w-17",
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
            "afterMs": 2500,
            "cards": []
          }
        ]
      },
      {
        "id": "w-18",
        "kind": "firstOf",
        "label": "Our CELL up (2) (2)",
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
        "id": "w-19",
        "kind": "firstOf",
        "label": "Our CELL up (2) (3)",
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
        "id": "w-20",
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
        "id": "p-21",
        "kind": "path",
        "lineId": "to-slide-sl-4",
        "park": false
      },
      {
        "id": "p-22",
        "kind": "path",
        "lineId": "to-slide-sr-5",
        "park": false
      },
      {
        "id": "w-23",
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
        "id": "w-24",
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
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "w-25",
        "kind": "firstOf",
        "label": "Fire until it tips (2) (2)",
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
        "id": "p-26",
        "kind": "path",
        "lineId": "to-start-s-6",
        "park": false
      },
      {
        "id": "w-27",
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
        "id": "w-28",
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
            "afterMs": 2500,
            "cards": []
          }
        ]
      },
      {
        "id": "w-29",
        "kind": "firstOf",
        "label": "Our CELL up (3) (2)",
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
        "id": "w-30",
        "kind": "firstOf",
        "label": "Our CELL up (3) (3)",
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
        "id": "w-31",
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
            "afterMs": 2500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-32",
        "kind": "path",
        "lineId": "to-slide-sl-7",
        "park": false
      },
      {
        "id": "p-33",
        "kind": "path",
        "lineId": "to-slide-sr-8",
        "park": false
      },
      {
        "id": "w-34",
        "kind": "firstOf",
        "label": "Sweep (3)",
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
        "id": "w-35",
        "kind": "firstOf",
        "label": "Fire until it tips (3)",
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
        "id": "w-36",
        "kind": "firstOf",
        "label": "Fire until it tips (3) (2)",
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
        "id": "p-37",
        "kind": "path",
        "lineId": "to-park-a-9",
        "park": true
      }
    ]
  },
  "version": "1.5.0"
}