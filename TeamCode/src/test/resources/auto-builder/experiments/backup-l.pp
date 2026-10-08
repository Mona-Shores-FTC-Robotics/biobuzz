{
  "startPoint": {
    "x": 59,
    "y": 133.69,
    "name": "START",
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-l-turn-1",
      "color": "#3cc8e4",
      "name": "START to L_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57,
        "y": 36
      },
      "controlPoints": [
        {
          "x": 57.5,
          "y": 100
        },
        {
          "x": 57.5,
          "y": 50
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.88,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.88,
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
      "id": "to-fire1-2",
      "color": "#3cc8e4",
      "name": "L_TURN to FIRE1",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 30
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-garden-in-3",
      "color": "#3cc8e4",
      "name": "FIRE1 to GARDEN_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 9.5,
        "y": 20.56
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.2,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
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
      "id": "to-garden-4",
      "color": "#3cc8e4",
      "name": "GARDEN_IN to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 9.5,
        "y": 10.96
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-r-w-5",
      "color": "#3cc8e4",
      "name": "GARDEN to R_W",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 30,
        "y": 40
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.1,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-r-n-6",
      "color": "#3cc8e4",
      "name": "R_W to R_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 32,
        "y": 104
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-far-flower-turn-7",
      "color": "#3cc8e4",
      "name": "R_N to FAR_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 118.34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-far-flower-8",
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
      "id": "to-l-n-9",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to L_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 52,
        "y": 119
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
      "id": "to-park-l-10",
      "color": "#3cc8e4",
      "name": "L_N to PARK_L",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 92
      },
      "controlPoints": [
        {
          "x": 40,
          "y": 100
        },
        {
          "x": 25,
          "y": 92
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
      "lineId": "to-l-turn-1"
    },
    {
      "kind": "path",
      "lineId": "to-fire1-2"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-3"
    },
    {
      "kind": "path",
      "lineId": "to-garden-4"
    },
    {
      "kind": "path",
      "lineId": "to-r-w-5"
    },
    {
      "kind": "path",
      "lineId": "to-r-n-6"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-turn-7"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-8"
    },
    {
      "kind": "path",
      "lineId": "to-l-n-9"
    },
    {
      "kind": "path",
      "lineId": "to-park-l-10"
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
    "exportName": "backup-l",
    "registry": {
      "actions": [
        "SpinUp",
        "LaunchAll"
      ],
      "conditions": [
        "Empty",
        "IntakeFull"
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
        133.69,
        270
      ],
      "L_TURN": [
        57,
        36,
        90
      ],
      "FIRE1": [
        55,
        30,
        90
      ],
      "L_N": [
        52,
        119,
        270
      ],
      "PARK_L": [
        10.5,
        92,
        270
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
      "GARDEN_IN": [
        9.5,
        20.56,
        270
      ],
      "GARDEN": [
        9.5,
        10.96,
        270
      ],
      "R_W": [
        30,
        40,
        90
      ],
      "R_N": [
        32,
        104,
        90
      ]
    },
    "pathEnds": {
      "to-l-turn-1": "L_TURN",
      "to-fire1-2": "FIRE1",
      "to-garden-in-3": "GARDEN_IN",
      "to-garden-4": "GARDEN",
      "to-r-w-5": "R_W",
      "to-r-n-6": "R_N",
      "to-far-flower-turn-7": "FAR_FLOWER_TURN",
      "to-far-flower-8": "FAR_FLOWER",
      "to-l-n-9": "L_N",
      "to-park-l-10": "PARK_L"
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
        "label": "The right partner launches and leaves",
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
        ]
      },
      {
        "id": "p-3",
        "kind": "path",
        "lineId": "to-l-turn-1",
        "park": false
      },
      {
        "id": "p-4",
        "kind": "path",
        "lineId": "to-fire1-2",
        "park": false
      },
      {
        "id": "w-5",
        "kind": "firstOf",
        "label": "Preloads at the right CELL (TIP 1, or spent if the partner's tipped it)",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 3000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-6",
        "kind": "path",
        "lineId": "to-garden-in-3",
        "park": false
      },
      {
        "id": "p-7",
        "kind": "path",
        "lineId": "to-garden-4",
        "park": false
      },
      {
        "id": "w-8",
        "kind": "firstOf",
        "label": "The GARDEN's 4",
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
        "id": "p-9",
        "kind": "path",
        "lineId": "to-r-w-5",
        "park": false
      },
      {
        "id": "p-10",
        "kind": "path",
        "lineId": "to-r-n-6",
        "park": false
      },
      {
        "id": "w-11",
        "kind": "firstOf",
        "label": "The GARDEN's 4 at the left CELL",
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
        "id": "p-12",
        "kind": "path",
        "lineId": "to-far-flower-turn-7",
        "park": false
      },
      {
        "id": "p-13",
        "kind": "path",
        "lineId": "to-far-flower-8",
        "park": false
      },
      {
        "id": "w-14",
        "kind": "firstOf",
        "label": "The far FLOWER's 4",
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
        "id": "p-15",
        "kind": "path",
        "lineId": "to-l-n-9",
        "park": false
      },
      {
        "id": "w-16",
        "kind": "firstOf",
        "label": "The far FLOWER's 4 at the left CELL (TIP 2)",
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
        "id": "p-17",
        "kind": "path",
        "lineId": "to-park-l-10",
        "park": true
      }
    ]
  },
  "version": "1.5.0"
}