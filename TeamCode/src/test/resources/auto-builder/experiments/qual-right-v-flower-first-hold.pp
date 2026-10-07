{
  "startPoint": {
    "x": 59,
    "y": 133.69,
    "name": "START",
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-far-flower-turn-1",
      "color": "#3cc8e4",
      "name": "START to FAR_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 118.34
      },
      "controlPoints": [
        {
          "x": 59,
          "y": 118.34
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.5,
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
      "id": "to-far-flower-2",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER_TURN to FAR_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 126.64
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-n-fire-3",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to N_FIRE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 114
      },
      "controlPoints": [
        {
          "x": 47.36,
          "y": 116.64
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.3,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-s-fire-4",
      "color": "#3cc8e4",
      "name": "N_FIRE to S_FIRE",
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
      "id": "to-sweep-e-5",
      "color": "#3cc8e4",
      "name": "S_FIRE to SWEEP_E",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 12
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 180
      }
    },
    {
      "id": "to-sweep-w-6",
      "color": "#3cc8e4",
      "name": "SWEEP_E to SWEEP_W",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 22,
        "y": 12
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-garden-7",
      "color": "#3cc8e4",
      "name": "SWEEP_W to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 9.5,
        "y": 10.959999999999999
      },
      "controlPoints": [
        {
          "x": 12,
          "y": 20
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.15,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 180
              }
            },
            {
              "startProgress": 0.15,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.6,
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
      "id": "to-s-fire-8",
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
      "id": "to-park-9",
      "color": "#3cc8e4",
      "name": "GARDEN to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13,
        "y": 87.44
      },
      "controlPoints": [
        {
          "x": 24,
          "y": 30
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
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
      "lineId": "to-far-flower-turn-1"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-2"
    },
    {
      "kind": "path",
      "lineId": "to-n-fire-3"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-4"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-e-5"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-w-6"
    },
    {
      "kind": "path",
      "lineId": "to-garden-7"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-8"
    },
    {
      "kind": "path",
      "lineId": "to-park-9"
    }
  ],
  "settings": {
    "xVelocity": 75,
    "yVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rWidth": 15.12,
    "rHeight": 15.12,
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
    "exportName": "qual-right-v-flower-first-hold",
    "registry": {
      "actions": [
        "SpinUp",
        "LaunchAll",
        "LaunchOne"
      ],
      "conditions": [
        "LeftCellUp",
        "Empty",
        "IntakeFull",
        "Tip",
        "RightCellUp"
      ],
      "typicalS": {
        "SpinUp": 0.1,
        "LaunchAll": 2.0,
        "LaunchOne": 0.5
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START": [
        59,
        133.69,
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
        20.56,
        270
      ],
      "GARDEN": [
        9.5,
        10.959999999999999,
        270
      ],
      "PARK": [
        13,
        87.44,
        90
      ],
      "FAR_FLOWER": [
        47.36,
        126.64,
        90
      ],
      "FAR_FLOWER_IN": [
        47.36,
        120.84,
        90
      ],
      "FAR_FLOWER_TURN": [
        47.36,
        118.34,
        90
      ],
      "S_FIRE": [
        57.5,
        24,
        90
      ],
      "N_FIRE": [
        57.5,
        114,
        270
      ],
      "WALL_FLOWER": [
        14.86,
        47.36,
        180
      ],
      "WALL_FLOWER_IN": [
        20.66,
        47.36,
        180
      ],
      "WALL_FLOWER_TURN": [
        23.16,
        47.36,
        180
      ],
      "SWEEP_E": [
        57.5,
        12,
        180
      ],
      "SWEEP_W": [
        22,
        12,
        180
      ]
    },
    "pathEnds": {
      "to-far-flower-turn-1": "FAR_FLOWER_TURN",
      "to-far-flower-2": "FAR_FLOWER",
      "to-n-fire-3": "N_FIRE",
      "to-s-fire-4": "S_FIRE",
      "to-sweep-e-5": "SWEEP_E",
      "to-sweep-w-6": "SWEEP_W",
      "to-garden-7": "GARDEN",
      "to-s-fire-8": "S_FIRE",
      "to-park-9": "PARK"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "a-21",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "p-22",
        "kind": "path",
        "lineId": "to-far-flower-turn-1",
        "park": false
      },
      {
        "id": "p-23",
        "kind": "path",
        "lineId": "to-far-flower-2",
        "park": false
      },
      {
        "id": "w-25",
        "kind": "firstOf",
        "label": "TIP 1 (the partner), seated at the FLOWER",
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
        "id": "a-26",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "a-27",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "a-28",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "a-29",
        "kind": "action",
        "name": "LaunchOne"
      },
      {
        "id": "w-30",
        "kind": "firstOf",
        "label": "The far FLOWER's 4, collected",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": []
          }
        ]
      },
      {
        "id": "p-31",
        "kind": "path",
        "lineId": "to-n-fire-3",
        "park": false
      },
      {
        "id": "w-32",
        "kind": "firstOf",
        "label": "Fire the far FLOWER's 4, lined up (TIP 2)",
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
        "id": "w-33",
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
        "id": "w-34",
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
            "afterMs": 200,
            "cards": []
          }
        ]
      },
      {
        "id": "p-35",
        "kind": "path",
        "lineId": "to-s-fire-4",
        "park": false
      },
      {
        "id": "w-36",
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
        "id": "p-37",
        "kind": "path",
        "lineId": "to-sweep-e-5",
        "park": false
      },
      {
        "id": "p-38",
        "kind": "path",
        "lineId": "to-sweep-w-6",
        "park": false
      },
      {
        "id": "p-39",
        "kind": "path",
        "lineId": "to-garden-7",
        "park": false
      },
      {
        "id": "w-40",
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
        "id": "p-41",
        "kind": "path",
        "lineId": "to-s-fire-8",
        "park": false
      },
      {
        "id": "w-42",
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
        "id": "p-43",
        "kind": "path",
        "lineId": "to-park-9",
        "park": true
      }
    ]
  },
  "version": "1.5.0"
}