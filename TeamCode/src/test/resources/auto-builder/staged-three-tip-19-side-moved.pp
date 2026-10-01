{
  "startPoint": {
    "x": 59,
    "y": 9.5,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-tunnel-1",
      "color": "#3cc8e4",
      "name": "START to TUNNEL",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 100
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-row-in-2",
      "color": "#3cc8e4",
      "name": "TUNNEL to ROW_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 29.6,
        "y": 114
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-left-shot-3",
      "color": "#3cc8e4",
      "name": "ROW_BACK to LEFT_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40,
        "y": 116
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.4,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.4,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 301
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-far-flower-turn-4",
      "color": "#3cc8e4",
      "name": "LEFT_SHOT to FAR_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 119.29
      },
      "controlPoints": [
        {
          "x": 40,
          "y": 119.29
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
                "degrees": 301
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 301,
                "endDeg": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-far-flower-5",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER_TURN to FAR_FLOWER",
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
      "id": "to-left-shot-6",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to LEFT_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 40,
        "y": 116
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.4,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.4,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 301
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-right-plunge-in-7",
      "color": "#3cc8e4",
      "name": "LEFT_SHOT to RIGHT_PLUNGE_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 24
      },
      "controlPoints": [
        {
          "x": 34,
          "y": 92
        },
        {
          "x": 12,
          "y": 60
        },
        {
          "x": 22,
          "y": 30
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
                "startDeg": 301,
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
      "id": "to-right-plunge-8",
      "color": "#3cc8e4",
      "name": "RIGHT_PLUNGE_IN to RIGHT_PLUNGE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 10.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      }
    },
    {
      "id": "to-right-shot-9",
      "color": "#3cc8e4",
      "name": "RIGHT_PLUNGE to RIGHT_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 36,
        "y": 30
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 49
              }
            },
            {
              "startProgress": 0.65,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 49
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-garden-10",
      "color": "#3cc8e4",
      "name": "RIGHT_SHOT to GARDEN",
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
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 49,
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
      "id": "to-right-shot-11",
      "color": "#3cc8e4",
      "name": "GARDEN to RIGHT_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 36,
        "y": 30
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 49
              }
            },
            {
              "startProgress": 0.65,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 49
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-right-shot-12",
      "color": "#3cc8e4",
      "name": "LEFT_SHOT to RIGHT_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 36,
        "y": 30
      },
      "controlPoints": [
        {
          "x": 34,
          "y": 92
        },
        {
          "x": 12,
          "y": 60
        },
        {
          "x": 22,
          "y": 30
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
                "startDeg": 301,
                "endDeg": 49
              }
            },
            {
              "startProgress": 0.65,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 49
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-right-plunge-in-13",
      "color": "#3cc8e4",
      "name": "RIGHT_SHOT to RIGHT_PLUNGE_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 24
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 49,
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
      "id": "to-right-plunge-14",
      "color": "#3cc8e4",
      "name": "RIGHT_PLUNGE_IN to RIGHT_PLUNGE",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 10.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      }
    },
    {
      "id": "to-right-shot-15",
      "color": "#3cc8e4",
      "name": "RIGHT_PLUNGE to RIGHT_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 36,
        "y": 30
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 49
              }
            },
            {
              "startProgress": 0.65,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 49
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-garden-16",
      "color": "#3cc8e4",
      "name": "RIGHT_SHOT to GARDEN",
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
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 49,
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
      "id": "to-right-shot-17",
      "color": "#3cc8e4",
      "name": "GARDEN to RIGHT_SHOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 36,
        "y": 30
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.65,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 49
              }
            },
            {
              "startProgress": 0.65,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 49
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-18",
      "color": "#3cc8e4",
      "name": "RIGHT_SHOT to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 15,
        "y": 89.5
      },
      "controlPoints": [
        {
          "x": 22,
          "y": 40
        },
        {
          "x": 22,
          "y": 80
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
                "startDeg": 49,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.65,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
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
      "lineId": "to-tunnel-1"
    },
    {
      "kind": "path",
      "lineId": "to-row-in-2"
    },
    {
      "kind": "path",
      "lineId": "to-left-shot-3"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-turn-4"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-5"
    },
    {
      "kind": "path",
      "lineId": "to-left-shot-6"
    },
    {
      "kind": "path",
      "lineId": "to-right-plunge-in-7"
    },
    {
      "kind": "path",
      "lineId": "to-right-plunge-8"
    },
    {
      "kind": "path",
      "lineId": "to-right-shot-9"
    },
    {
      "kind": "path",
      "lineId": "to-garden-10"
    },
    {
      "kind": "path",
      "lineId": "to-right-shot-11"
    },
    {
      "kind": "path",
      "lineId": "to-right-shot-12"
    },
    {
      "kind": "path",
      "lineId": "to-right-plunge-in-13"
    },
    {
      "kind": "path",
      "lineId": "to-right-plunge-14"
    },
    {
      "kind": "path",
      "lineId": "to-right-shot-15"
    },
    {
      "kind": "path",
      "lineId": "to-garden-16"
    },
    {
      "kind": "path",
      "lineId": "to-right-shot-17"
    },
    {
      "kind": "path",
      "lineId": "to-park-18"
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
    "exportName": "staged-three-tip-19-side-moved",
    "registry": {
      "actions": [
        "LaunchAll",
        "CollectSeen"
      ],
      "conditions": [
        "Empty",
        "IntakeFull",
        "RightCellUp",
        "LeftCellUp"
      ],
      "typicalS": {
        "LaunchAll": 2.0,
        "CollectSeen": 2.0
      },
      "events": []
    },
    "points": {
      "START": [
        59,
        9.5,
        90
      ],
      "WALL_FLOWER": [
        13.91,
        47.36,
        180
      ],
      "WALL_FLOWER_IN": [
        19.71,
        47.36,
        180
      ],
      "WALL_FLOWER_TURN": [
        22.21,
        47.36,
        180
      ],
      "LEFT_SHOT": [
        40,
        116,
        301
      ],
      "FAR_FLOWER": [
        47.36,
        127.59,
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
      "RIGHT_PLUNGE_IN": [
        55,
        24,
        270
      ],
      "RIGHT_PLUNGE": [
        55,
        10.5,
        270
      ],
      "RIGHT_SHOT": [
        36,
        30,
        49
      ],
      "GARDEN": [
        8.5,
        11,
        270
      ],
      "PARK": [
        15,
        89.5,
        90
      ],
      "TUNNEL": [
        57.5,
        100,
        90
      ],
      "ROW_IN": [
        29.6,
        114,
        90
      ],
      "ROW_BACK": [
        29.6,
        116,
        90
      ]
    },
    "pathEnds": {
      "to-tunnel-1": "TUNNEL",
      "to-row-in-2": "ROW_IN",
      "to-left-shot-3": "LEFT_SHOT",
      "to-far-flower-turn-4": "FAR_FLOWER_TURN",
      "to-far-flower-5": "FAR_FLOWER",
      "to-left-shot-6": "LEFT_SHOT",
      "to-right-plunge-in-7": "RIGHT_PLUNGE_IN",
      "to-right-plunge-8": "RIGHT_PLUNGE",
      "to-right-shot-9": "RIGHT_SHOT",
      "to-garden-10": "GARDEN",
      "to-right-shot-11": "RIGHT_SHOT",
      "to-right-shot-12": "RIGHT_SHOT",
      "to-right-plunge-in-13": "RIGHT_PLUNGE_IN",
      "to-right-plunge-14": "RIGHT_PLUNGE",
      "to-right-shot-15": "RIGHT_SHOT",
      "to-garden-16": "GARDEN",
      "to-right-shot-17": "RIGHT_SHOT",
      "to-park-18": "PARK"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "w-1",
        "kind": "firstOf",
        "label": "Fire all 4 preloads (TIP 1)",
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
        "id": "p-2",
        "kind": "path",
        "lineId": "to-tunnel-1",
        "park": false
      },
      {
        "id": "p-3",
        "kind": "path",
        "lineId": "to-row-in-2",
        "park": false
      },
      {
        "id": "w-4",
        "kind": "firstOf",
        "label": "Pick up the partner's row",
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
        ],
        "alongside": "CollectSeen"
      },
      {
        "id": "p-5",
        "kind": "path",
        "lineId": "to-left-shot-3",
        "park": false
      },
      {
        "id": "a-6",
        "kind": "action",
        "name": "LaunchAll"
      },
      {
        "id": "p-7",
        "kind": "path",
        "lineId": "to-far-flower-turn-4",
        "park": false
      },
      {
        "id": "p-8",
        "kind": "path",
        "lineId": "to-far-flower-5",
        "park": false
      },
      {
        "id": "w-9",
        "kind": "firstOf",
        "label": "Collect at FAR_FLOWER",
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
        "id": "p-10",
        "kind": "path",
        "lineId": "to-left-shot-6",
        "park": false
      },
      {
        "id": "w-33",
        "kind": "firstOf",
        "label": "Did a partner make TIP 2?",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [
              {
                "id": "p-21",
                "kind": "path",
                "lineId": "to-right-shot-12",
                "park": false
              },
              {
                "id": "a-22",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "p-23",
                "kind": "path",
                "lineId": "to-right-plunge-in-13",
                "park": false
              },
              {
                "id": "p-24",
                "kind": "path",
                "lineId": "to-right-plunge-14",
                "park": false
              },
              {
                "id": "w-25",
                "kind": "firstOf",
                "label": "Spilled NECTAR (B)",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 600,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-26",
                "kind": "path",
                "lineId": "to-right-shot-15",
                "park": false
              },
              {
                "id": "w-27",
                "kind": "firstOf",
                "label": "Fire (B)",
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
                "id": "w-32",
                "kind": "firstOf",
                "label": "TIP 3 yet?",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": [],
                    "label": "Yes: park"
                  },
                  {
                    "afterMs": 1500,
                    "cards": [
                      {
                        "id": "p-28",
                        "kind": "path",
                        "lineId": "to-garden-16",
                        "park": false
                      },
                      {
                        "id": "w-29",
                        "kind": "firstOf",
                        "label": "Collect in the GARDEN (B)",
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
                        "id": "p-30",
                        "kind": "path",
                        "lineId": "to-right-shot-17",
                        "park": false
                      },
                      {
                        "id": "w-31",
                        "kind": "firstOf",
                        "label": "Fire the TIP 3 volley, then park",
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
                      }
                    ],
                    "label": "No: the GARDEN"
                  }
                ]
              }
            ],
            "label": "Yes: right CELL up"
          },
          {
            "afterMs": 50,
            "cards": [
              {
                "id": "w-11",
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
                    "afterMs": 2500,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              },
              {
                "id": "p-12",
                "kind": "path",
                "lineId": "to-right-plunge-in-7",
                "park": false
              },
              {
                "id": "p-13",
                "kind": "path",
                "lineId": "to-right-plunge-8",
                "park": false
              },
              {
                "id": "w-14",
                "kind": "firstOf",
                "label": "Spilled NECTAR",
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
                "id": "p-15",
                "kind": "path",
                "lineId": "to-right-shot-9",
                "park": false
              },
              {
                "id": "a-16",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "p-17",
                "kind": "path",
                "lineId": "to-garden-10",
                "park": false
              },
              {
                "id": "w-18",
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
                    "afterMs": 1000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-19",
                "kind": "path",
                "lineId": "to-right-shot-11",
                "park": false
              },
              {
                "id": "w-20",
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
                    "afterMs": 2500,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              }
            ],
            "label": "No: TIP 2 is ours"
          }
        ]
      },
      {
        "id": "p-34",
        "kind": "path",
        "lineId": "to-park-18",
        "park": true
      }
    ]
  },
  "version": "1.5.0"
}