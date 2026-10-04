{
  "startPoint": {
    "x": 59,
    "y": 9.5,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-catch-r-1",
      "color": "#3cc8e4",
      "name": "START to CATCH_R",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 33
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-catch-r-back-2",
      "color": "#3cc8e4",
      "name": "CATCH_R to CATCH_R_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 30
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-l-exit-3",
      "color": "#3cc8e4",
      "name": "CATCH_R_BACK to L_EXIT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 56.5,
        "y": 103
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-l-home-4",
      "color": "#3cc8e4",
      "name": "L_EXIT to L_HOME",
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
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.4,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 270
              }
            },
            {
              "startProgress": 0.4,
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
      "id": "to-catch-l-5",
      "color": "#3cc8e4",
      "name": "L_HOME to CATCH_L",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 108.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      }
    },
    {
      "id": "to-catch-l-back-6",
      "color": "#3cc8e4",
      "name": "CATCH_L to CATCH_L_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 111.5
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      }
    },
    {
      "id": "to-l-exit-back-7",
      "color": "#3cc8e4",
      "name": "CATCH_L_BACK to L_EXIT_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 56.5,
        "y": 103
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-r-exit-back-8",
      "color": "#3cc8e4",
      "name": "L_EXIT_BACK to R_EXIT_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 34
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-shot-a-9",
      "color": "#3cc8e4",
      "name": "R_EXIT_BACK to SHOT_A",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 34,
        "y": 20
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
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 60
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 60
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-shot-g-10",
      "color": "#3cc8e4",
      "name": "SHOT_A to SHOT_G",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 22,
        "y": 22
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
                "startDeg": 60,
                "endDeg": 50
              }
            },
            {
              "startProgress": 0.65,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 50
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-garden-in-11",
      "color": "#3cc8e4",
      "name": "SHOT_A to GARDEN_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 22
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
                "startDeg": 60,
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
      "id": "to-garden-12",
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
      "id": "to-shot-g-13",
      "color": "#3cc8e4",
      "name": "GARDEN to SHOT_G",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 22,
        "y": 22
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
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 50
              }
            },
            {
              "startProgress": 0.9,
              "endProgress": 1,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 50
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-14",
      "color": "#3cc8e4",
      "name": "SHOT_G to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 15,
        "y": 90
      },
      "controlPoints": [
        {
          "x": 20,
          "y": 60
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
                "startDeg": 50,
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
    },
    {
      "id": "to-flower-l-turn-15",
      "color": "#3cc8e4",
      "name": "L_HOME to FLOWER_L_TURN",
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
          "x": 59,
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
      "id": "to-flower-l-16",
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
      "id": "to-flower-l-back-l-home-17",
      "color": "#3cc8e4",
      "name": "FLOWER_L to FLOWER_L_BACK_L_HOME",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 119.29
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.5,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.5,
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
      "id": "to-l-home-18",
      "color": "#3cc8e4",
      "name": "FLOWER_L_BACK_L_HOME to L_HOME",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 131.75
      },
      "controlPoints": [
        {
          "x": 59,
          "y": 121.29
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-park-l-19",
      "color": "#3cc8e4",
      "name": "L_HOME to PARK_L",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 15,
        "y": 89
      },
      "controlPoints": [
        {
          "x": 59,
          "y": 104
        },
        {
          "x": 59,
          "y": 104
        },
        {
          "x": 32,
          "y": 110
        },
        {
          "x": 32,
          "y": 110
        },
        {
          "x": 32,
          "y": 86
        },
        {
          "x": 32,
          "y": 86
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
      "lineId": "to-catch-r-1"
    },
    {
      "kind": "path",
      "lineId": "to-catch-r-back-2"
    },
    {
      "kind": "path",
      "lineId": "to-l-exit-3"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-4"
    },
    {
      "kind": "path",
      "lineId": "to-catch-l-5"
    },
    {
      "kind": "path",
      "lineId": "to-catch-l-back-6"
    },
    {
      "kind": "path",
      "lineId": "to-l-exit-back-7"
    },
    {
      "kind": "path",
      "lineId": "to-r-exit-back-8"
    },
    {
      "kind": "path",
      "lineId": "to-shot-a-9"
    },
    {
      "kind": "path",
      "lineId": "to-shot-g-10"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-11"
    },
    {
      "kind": "path",
      "lineId": "to-garden-12"
    },
    {
      "kind": "path",
      "lineId": "to-shot-g-13"
    },
    {
      "kind": "path",
      "lineId": "to-park-14"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-turn-15"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-16"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-back-l-home-17"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-18"
    },
    {
      "kind": "path",
      "lineId": "to-park-l-19"
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
    "exportName": "solo-catch",
    "registry": {
      "actions": [
        "LaunchAll",
        "CollectSeen"
      ],
      "conditions": [
        "Empty",
        "IntakeFull",
        "LeftCellUp",
        "RightCellUp",
        "Tip"
      ],
      "typicalS": {
        "LaunchAll": 2.0,
        "CollectSeen": 2.0
      },
      "events": [
        "Tip"
      ]
    },
    "points": {
      "START": [
        59,
        9.5,
        90
      ],
      "L_HOME": [
        59,
        131.75,
        270
      ],
      "CATCH_R": [
        57.5,
        33,
        90
      ],
      "CATCH_L": [
        57.5,
        108.5,
        270
      ],
      "L_EXIT": [
        56.5,
        103,
        90
      ],
      "L_EXIT_BACK": [
        56.5,
        103,
        270
      ],
      "R_EXIT_BACK": [
        57.5,
        34,
        270
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
      "PARK": [
        15,
        90,
        90
      ],
      "CATCH_R_BACK": [
        57.5,
        30,
        90
      ],
      "CATCH_L_BACK": [
        57.5,
        111.5,
        270
      ],
      "SHOT_A": [
        34,
        20,
        60
      ],
      "SHOT_G": [
        22,
        22,
        50
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
      "FLOWER_L_BACK_L_HOME": [
        57.5,
        119.29,
        270
      ],
      "PARK_L": [
        15,
        89,
        270
      ]
    },
    "pathEnds": {
      "to-catch-r-1": "CATCH_R",
      "to-catch-r-back-2": "CATCH_R_BACK",
      "to-l-exit-3": "L_EXIT",
      "to-l-home-4": "L_HOME",
      "to-catch-l-5": "CATCH_L",
      "to-catch-l-back-6": "CATCH_L_BACK",
      "to-l-exit-back-7": "L_EXIT_BACK",
      "to-r-exit-back-8": "R_EXIT_BACK",
      "to-shot-a-9": "SHOT_A",
      "to-shot-g-10": "SHOT_G",
      "to-garden-in-11": "GARDEN_IN",
      "to-garden-12": "GARDEN",
      "to-shot-g-13": "SHOT_G",
      "to-park-14": "PARK",
      "to-flower-l-turn-15": "FLOWER_L_TURN",
      "to-flower-l-16": "FLOWER_L",
      "to-flower-l-back-l-home-17": "FLOWER_L_BACK_L_HOME",
      "to-l-home-18": "L_HOME",
      "to-park-l-19": "PARK_L"
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
            "afterMs": 4500,
            "cards": []
          }
        ],
        "alongside": "LaunchAll"
      },
      {
        "id": "p-2",
        "kind": "path",
        "lineId": "to-catch-r-1",
        "park": false
      },
      {
        "id": "w-3",
        "kind": "firstOf",
        "label": "TIP 1 spill: it lands",
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
        "id": "w-4",
        "kind": "firstOf",
        "label": "TIP 1 spill: what lies near",
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
        ],
        "alongside": "CollectSeen"
      },
      {
        "id": "p-5",
        "kind": "path",
        "lineId": "to-catch-r-back-2",
        "park": false
      },
      {
        "id": "p-6",
        "kind": "path",
        "lineId": "to-l-exit-3",
        "park": false
      },
      {
        "id": "p-7",
        "kind": "path",
        "lineId": "to-l-home-4",
        "park": false
      },
      {
        "id": "w-31",
        "kind": "firstOf",
        "label": "Fire at the left CELL (TIP 2)",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": [
              {
                "id": "p-8",
                "kind": "path",
                "lineId": "to-catch-l-5",
                "park": false
              },
              {
                "id": "w-9",
                "kind": "firstOf",
                "label": "TIP 2 spill: it lands",
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
                "id": "w-10",
                "kind": "firstOf",
                "label": "TIP 2 spill: what lies near",
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
                ],
                "alongside": "CollectSeen"
              },
              {
                "id": "p-11",
                "kind": "path",
                "lineId": "to-catch-l-back-6",
                "park": false
              },
              {
                "id": "p-12",
                "kind": "path",
                "lineId": "to-l-exit-back-7",
                "park": false
              },
              {
                "id": "p-13",
                "kind": "path",
                "lineId": "to-r-exit-back-8",
                "park": false
              },
              {
                "id": "p-14",
                "kind": "path",
                "lineId": "to-shot-a-9",
                "park": false
              },
              {
                "id": "w-15",
                "kind": "firstOf",
                "label": "Fire the TIP 2 spill (TIP 3)",
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
                "id": "w-22",
                "kind": "firstOf",
                "label": "TIP 3 yet?",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": [
                      {
                        "id": "p-16",
                        "kind": "path",
                        "lineId": "to-shot-g-10",
                        "park": false
                      }
                    ],
                    "label": "Yes"
                  },
                  {
                    "afterMs": 100,
                    "cards": [
                      {
                        "id": "p-17",
                        "kind": "path",
                        "lineId": "to-garden-in-11",
                        "park": false
                      },
                      {
                        "id": "p-18",
                        "kind": "path",
                        "lineId": "to-garden-12",
                        "park": false
                      },
                      {
                        "id": "w-19",
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
                            "afterMs": 1500,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "p-20",
                        "kind": "path",
                        "lineId": "to-shot-g-13",
                        "park": false
                      },
                      {
                        "id": "w-21",
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
                            "afterMs": 1800,
                            "cards": []
                          }
                        ],
                        "alongside": "LaunchAll"
                      }
                    ],
                    "label": "No: the GARDEN"
                  }
                ]
              },
              {
                "id": "p-23",
                "kind": "path",
                "lineId": "to-park-14",
                "park": true
              }
            ],
            "label": "It's going over"
          },
          {
            "afterMs": 2500,
            "cards": [
              {
                "id": "p-24",
                "kind": "path",
                "lineId": "to-flower-l-turn-15",
                "park": false
              },
              {
                "id": "p-25",
                "kind": "path",
                "lineId": "to-flower-l-16",
                "park": false
              },
              {
                "id": "w-26",
                "kind": "firstOf",
                "label": "Top up at the far FLOWER",
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
                "id": "p-27",
                "kind": "path",
                "lineId": "to-flower-l-back-l-home-17",
                "park": false
              },
              {
                "id": "p-28",
                "kind": "path",
                "lineId": "to-l-home-18",
                "park": false
              },
              {
                "id": "w-29",
                "kind": "firstOf",
                "label": "Fire again (TIP 2)",
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
                "id": "p-30",
                "kind": "path",
                "lineId": "to-park-l-19",
                "park": true
              }
            ],
            "label": "Not yet: top up"
          }
        ],
        "alongside": "LaunchAll"
      }
    ]
  },
  "version": "1.5.0"
}