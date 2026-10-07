{
  "startPoint": {
    "x": 59,
    "y": 133.69,
    "name": "START",
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-n-fire-1",
      "color": "#3cc8e4",
      "name": "START to N_FIRE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 114
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-far-flower-turn-2",
      "color": "#3cc8e4",
      "name": "N_FIRE to FAR_FLOWER_TURN",
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
          "x": 57.5,
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
      "id": "to-far-flower-3",
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
      "id": "to-west-via-4",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to WEST_VIA",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 35,
        "y": 96
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
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 225
              }
            },
            {
              "startProgress": 0.8,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 225
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-turn-5",
      "color": "#3cc8e4",
      "name": "WEST_VIA to WALL_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 23.16,
        "y": 47.36
      },
      "controlPoints": [
        {
          "x": 29,
          "y": 62
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-wall-flower-6",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER_TURN to WALL_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14.86,
        "y": 47.36
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
      "name": "WALL_FLOWER to GARDEN",
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
          "x": 24.86,
          "y": 47.36
        },
        {
          "x": 14.86,
          "y": 28
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
                "degrees": 180
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 0.7,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
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
      "name": "S_FIRE to PARK",
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
      "lineId": "to-n-fire-1"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-turn-2"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-3"
    },
    {
      "kind": "path",
      "lineId": "to-west-via-4"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-turn-5"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-6"
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
    "exportName": "qual-right-v-seatfire-west",
    "registry": {
      "actions": [
        "SpinUp",
        "LaunchAll",
        "StreamOn",
        "StreamOff"
      ],
      "conditions": [
        "LeftCellUp",
        "Empty",
        "IntakeFull",
        "Tip"
      ],
      "typicalS": {
        "SpinUp": 0.1,
        "LaunchAll": 2.0,
        "StreamOn": 1.0,
        "StreamOff": 1.0
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
      "WEST_VIA": [
        35,
        96,
        225
      ]
    },
    "pathEnds": {
      "to-n-fire-1": "N_FIRE",
      "to-far-flower-turn-2": "FAR_FLOWER_TURN",
      "to-far-flower-3": "FAR_FLOWER",
      "to-west-via-4": "WEST_VIA",
      "to-wall-flower-turn-5": "WALL_FLOWER_TURN",
      "to-wall-flower-6": "WALL_FLOWER",
      "to-garden-7": "GARDEN",
      "to-s-fire-8": "S_FIRE",
      "to-park-9": "PARK"
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
        "lineId": "to-n-fire-1",
        "park": false
      },
      {
        "id": "w-3",
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
        "id": "w-4",
        "kind": "firstOf",
        "label": "Fire the preloads at the left CELL",
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
        "id": "p-5",
        "kind": "path",
        "lineId": "to-far-flower-turn-2",
        "park": false
      },
      {
        "id": "p-6",
        "kind": "path",
        "lineId": "to-far-flower-3",
        "park": false
      },
      {
        "id": "a-8",
        "kind": "action",
        "name": "StreamOn"
      },
      {
        "id": "w-9",
        "kind": "firstOf",
        "label": "Extract and fire the far FLOWER (TIP 2)",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 3500,
            "cards": []
          }
        ]
      },
      {
        "id": "a-10",
        "kind": "action",
        "name": "StreamOff"
      },
      {
        "id": "p-11",
        "kind": "path",
        "lineId": "to-west-via-4",
        "park": false
      },
      {
        "id": "p-12",
        "kind": "path",
        "lineId": "to-wall-flower-turn-5",
        "park": false
      },
      {
        "id": "p-13",
        "kind": "path",
        "lineId": "to-wall-flower-6",
        "park": false
      },
      {
        "id": "a-14",
        "kind": "action",
        "name": "StreamOn"
      },
      {
        "id": "w-15",
        "kind": "firstOf",
        "label": "Extract and fire the wall FLOWER",
        "rows": [
          {
            "when": [
              "Tip"
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
        "id": "a-16",
        "kind": "action",
        "name": "StreamOff"
      },
      {
        "id": "p-17",
        "kind": "path",
        "lineId": "to-garden-7",
        "park": false
      },
      {
        "id": "w-18",
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
        "id": "p-19",
        "kind": "path",
        "lineId": "to-s-fire-8",
        "park": false
      },
      {
        "id": "w-20",
        "kind": "firstOf",
        "label": "Fire the GARDEN (TIP 3)",
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
        "id": "p-21",
        "kind": "path",
        "lineId": "to-park-9",
        "park": false
      }
    ]
  },
  "version": "1.5.0"
}