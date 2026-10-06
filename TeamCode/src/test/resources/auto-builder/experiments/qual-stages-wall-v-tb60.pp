{
  "startPoint": {
    "x": 59.0,
    "y": 8.12,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-s-catch-1",
      "color": "#3cc8e4",
      "name": "START to S_CATCH",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 28
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-n-turn-2",
      "color": "#3cc8e4",
      "name": "S_CATCH to N_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55.5,
        "y": 104
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.95,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.95,
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
      "id": "to-n-low-3",
      "color": "#3cc8e4",
      "name": "N_TURN to N_LOW",
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
      "id": "to-row-s-4",
      "color": "#3cc8e4",
      "name": "N_LOW to ROW_S",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 29.6,
        "y": 117.35
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.6,
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
      "id": "to-row-1-5",
      "color": "#3cc8e4",
      "name": "ROW_S to ROW_1",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 29.6,
        "y": 119.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-row-2-6",
      "color": "#3cc8e4",
      "name": "ROW_1 to ROW_2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 29.6,
        "y": 122.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-row-3-7",
      "color": "#3cc8e4",
      "name": "ROW_2 to ROW_3",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 29.6,
        "y": 126.15
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-row-4-8",
      "color": "#3cc8e4",
      "name": "ROW_3 to ROW_4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 29.6,
        "y": 128.95
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-row-n-9",
      "color": "#3cc8e4",
      "name": "ROW_4 to ROW_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 29.6,
        "y": 131.75
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-n-low-10",
      "color": "#3cc8e4",
      "name": "ROW_N to N_LOW",
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
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.9,
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
      "id": "to-far-flower-turn-11",
      "color": "#3cc8e4",
      "name": "N_LOW to FAR_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 120.67
      },
      "controlPoints": [
        {
          "x": 57.5,
          "y": 120.67
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
      "id": "to-far-flower-12",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER_TURN to FAR_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 128.97
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-n-fire-13",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to N_FIRE",
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
      "id": "to-s-fire-14",
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
              "endProgress": 0.87,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.87,
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
      "id": "to-garden-in-15",
      "color": "#3cc8e4",
      "name": "S_FIRE to GARDEN_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 20.62
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
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
      "id": "to-garden-16",
      "color": "#3cc8e4",
      "name": "GARDEN_IN to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 9.62
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-s-fire-17",
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
      "id": "to-s-fire-18",
      "color": "#3cc8e4",
      "name": "N_LOW to S_FIRE",
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
      "id": "to-garden-in-19",
      "color": "#3cc8e4",
      "name": "S_FIRE to GARDEN_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 20.62
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.6,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
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
      "id": "to-garden-20",
      "color": "#3cc8e4",
      "name": "GARDEN_IN to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 9.62
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-s-fire-21",
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
      "lineId": "to-s-catch-1"
    },
    {
      "kind": "path",
      "lineId": "to-n-turn-2"
    },
    {
      "kind": "path",
      "lineId": "to-n-low-3"
    },
    {
      "kind": "path",
      "lineId": "to-row-s-4"
    },
    {
      "kind": "path",
      "lineId": "to-row-1-5"
    },
    {
      "kind": "path",
      "lineId": "to-row-2-6"
    },
    {
      "kind": "path",
      "lineId": "to-row-3-7"
    },
    {
      "kind": "path",
      "lineId": "to-row-4-8"
    },
    {
      "kind": "path",
      "lineId": "to-row-n-9"
    },
    {
      "kind": "path",
      "lineId": "to-n-low-10"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-turn-11"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-12"
    },
    {
      "kind": "path",
      "lineId": "to-n-fire-13"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-14"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-15"
    },
    {
      "kind": "path",
      "lineId": "to-garden-16"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-17"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-18"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-19"
    },
    {
      "kind": "path",
      "lineId": "to-garden-20"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-21"
    }
  ],
  "settings": {
    "xVelocity": 75,
    "yVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rWidth": 15.24,
    "rHeight": 15.24,
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
    "exportName": "qual-stages-wall-v-tb60",
    "registry": {
      "actions": [
        "LaunchAll"
      ],
      "conditions": [
        "Empty",
        "LeftCellUp",
        "IntakeFull",
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 2.0
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START": [
        59.0,
        8.12,
        90
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
        55.5,
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
        20.62,
        270
      ],
      "GARDEN": [
        8.5,
        9.62,
        270
      ],
      "PARK": [
        13.0,
        87.38,
        90
      ],
      "FAR_FLOWER": [
        47.36,
        128.97,
        90
      ],
      "FAR_FLOWER_IN": [
        47.36,
        123.17,
        90
      ],
      "FAR_FLOWER_TURN": [
        47.36,
        120.67,
        90
      ],
      "WALL_FLOWER": [
        12.53,
        47.36,
        180
      ],
      "WALL_FLOWER_IN": [
        18.33,
        47.36,
        180
      ],
      "WALL_FLOWER_TURN": [
        20.83,
        47.36,
        180
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
      "ROW_S": [
        29.6,
        117.35,
        90
      ],
      "ROW_1": [
        29.6,
        119.75,
        90
      ],
      "ROW_2": [
        29.6,
        122.75,
        90
      ],
      "ROW_3": [
        29.6,
        126.15,
        90
      ],
      "ROW_4": [
        29.6,
        128.95,
        90
      ],
      "ROW_N": [
        29.6,
        131.75,
        90
      ]
    },
    "pathEnds": {
      "to-s-catch-1": "S_CATCH",
      "to-n-turn-2": "N_TURN",
      "to-n-low-3": "N_LOW",
      "to-row-s-4": "ROW_S",
      "to-row-1-5": "ROW_1",
      "to-row-2-6": "ROW_2",
      "to-row-3-7": "ROW_3",
      "to-row-4-8": "ROW_4",
      "to-row-n-9": "ROW_N",
      "to-n-low-10": "N_LOW",
      "to-far-flower-turn-11": "FAR_FLOWER_TURN",
      "to-far-flower-12": "FAR_FLOWER",
      "to-n-fire-13": "N_FIRE",
      "to-s-fire-14": "S_FIRE",
      "to-garden-in-15": "GARDEN_IN",
      "to-garden-16": "GARDEN",
      "to-s-fire-17": "S_FIRE",
      "to-s-fire-18": "S_FIRE",
      "to-garden-in-19": "GARDEN_IN",
      "to-garden-20": "GARDEN",
      "to-s-fire-21": "S_FIRE"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "w-1",
        "kind": "firstOf",
        "label": "Fire the preloads (TIP 1)",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 4000,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-4",
        "kind": "path",
        "lineId": "to-s-catch-1",
        "park": false
      },
      {
        "id": "w-2",
        "kind": "firstOf",
        "label": "TIP 1 settles",
        "rows": [
          {
            "when": [
              "LeftCellUp"
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
        "id": "w-3",
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
            "afterMs": 500,
            "cards": []
          }
        ]
      },
      {
        "id": "p-5",
        "kind": "path",
        "lineId": "to-n-turn-2",
        "park": false
      },
      {
        "id": "p-6",
        "kind": "path",
        "lineId": "to-n-low-3",
        "park": false
      },
      {
        "id": "w-7",
        "kind": "firstOf",
        "label": "Fire the catch",
        "rows": [
          {
            "when": [
              "Empty"
            ],
            "cards": []
          },
          {
            "afterMs": 2200,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-8",
        "kind": "path",
        "lineId": "to-row-s-4",
        "park": false
      },
      {
        "id": "p-9",
        "kind": "path",
        "lineId": "to-row-1-5",
        "park": false
      },
      {
        "id": "w-10",
        "kind": "firstOf",
        "label": "Row piece 1",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 450,
            "cards": []
          }
        ]
      },
      {
        "id": "p-11",
        "kind": "path",
        "lineId": "to-row-2-6",
        "park": false
      },
      {
        "id": "w-12",
        "kind": "firstOf",
        "label": "Row piece 2",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 450,
            "cards": []
          }
        ]
      },
      {
        "id": "p-13",
        "kind": "path",
        "lineId": "to-row-3-7",
        "park": false
      },
      {
        "id": "w-14",
        "kind": "firstOf",
        "label": "Row piece 3",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 450,
            "cards": []
          }
        ]
      },
      {
        "id": "p-15",
        "kind": "path",
        "lineId": "to-row-4-8",
        "park": false
      },
      {
        "id": "w-16",
        "kind": "firstOf",
        "label": "Row piece 4",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 450,
            "cards": []
          }
        ]
      },
      {
        "id": "p-17",
        "kind": "path",
        "lineId": "to-row-n-9",
        "park": false
      },
      {
        "id": "w-18",
        "kind": "firstOf",
        "label": "Row piece 5",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 450,
            "cards": []
          }
        ]
      },
      {
        "id": "p-19",
        "kind": "path",
        "lineId": "to-n-low-10",
        "park": false
      },
      {
        "id": "w-20",
        "kind": "firstOf",
        "label": "Fire the row",
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
        "id": "w-42",
        "kind": "firstOf",
        "label": "TIP 2?",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": [
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
                    "afterMs": 1300,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-35",
                "kind": "path",
                "lineId": "to-s-fire-18",
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
                "lineId": "to-garden-in-19",
                "park": false
              },
              {
                "id": "p-38",
                "kind": "path",
                "lineId": "to-garden-20",
                "park": false
              },
              {
                "id": "w-39",
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
                "id": "p-40",
                "kind": "path",
                "lineId": "to-s-fire-21",
                "park": false
              },
              {
                "id": "w-41",
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
              }
            ],
            "label": "Yes"
          },
          {
            "afterMs": 600,
            "cards": [
              {
                "id": "p-21",
                "kind": "path",
                "lineId": "to-far-flower-turn-11",
                "park": false
              },
              {
                "id": "p-22",
                "kind": "path",
                "lineId": "to-far-flower-12",
                "park": false
              },
              {
                "id": "w-23",
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
                "id": "p-24",
                "kind": "path",
                "lineId": "to-n-fire-13",
                "park": false
              },
              {
                "id": "w-25",
                "kind": "firstOf",
                "label": "Fire the far FLOWER (TIP 2)",
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
                "id": "w-26",
                "kind": "firstOf",
                "label": "It lands (B)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1300,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-27",
                "kind": "path",
                "lineId": "to-s-fire-14",
                "park": false
              },
              {
                "id": "w-28",
                "kind": "firstOf",
                "label": "Fire TIP 2's spill (B)",
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
                "id": "p-29",
                "kind": "path",
                "lineId": "to-garden-in-15",
                "park": false
              },
              {
                "id": "p-30",
                "kind": "path",
                "lineId": "to-garden-16",
                "park": false
              },
              {
                "id": "w-31",
                "kind": "firstOf",
                "label": "The GARDEN (B)",
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
                "id": "p-32",
                "kind": "path",
                "lineId": "to-s-fire-17",
                "park": false
              },
              {
                "id": "w-33",
                "kind": "firstOf",
                "label": "Fire the GARDEN (B)",
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
              }
            ],
            "label": "No: the far FLOWER"
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}