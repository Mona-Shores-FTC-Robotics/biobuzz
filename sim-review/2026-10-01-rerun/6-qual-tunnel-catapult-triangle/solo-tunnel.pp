{
  "startPoint": {
    "x": 59,
    "y": 9.5,
    "name": "START",
    "headingDeg": 90
  },
  "lines": [
    {
      "id": "to-l-exit-1",
      "color": "#3cc8e4",
      "name": "START to L_EXIT",
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
      "id": "to-l-home-2",
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
      "id": "to-l-exit-back-3",
      "color": "#3cc8e4",
      "name": "L_HOME to L_EXIT_BACK",
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
      "id": "to-r-exit-back-4",
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
      "id": "to-r-home-5",
      "color": "#3cc8e4",
      "name": "R_EXIT_BACK to R_HOME",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.5,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.5,
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
      "id": "to-garden-in-6",
      "color": "#3cc8e4",
      "name": "R_HOME to GARDEN_IN",
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
      "id": "to-garden-7",
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
      "id": "to-r-back-8",
      "color": "#3cc8e4",
      "name": "GARDEN to R_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 11
      },
      "controlPoints": [
        {
          "x": 20,
          "y": 18
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
                "startDeg": 270,
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
      "id": "to-park-9",
      "color": "#3cc8e4",
      "name": "R_HOME to PARK",
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
          "x": 24,
          "y": 24
        },
        {
          "x": 24,
          "y": 85
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-park-10",
      "color": "#3cc8e4",
      "name": "R_HOME to PARK",
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
          "x": 24,
          "y": 24
        },
        {
          "x": 24,
          "y": 85
        }
      ],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-row-in-11",
      "color": "#3cc8e4",
      "name": "L_HOME to ROW_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 34.6,
        "y": 112
      },
      "controlPoints": [
        {
          "x": 57.5,
          "y": 114
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.2,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.2,
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
      "id": "to-l-home-12",
      "color": "#3cc8e4",
      "name": "ROW_IN to L_HOME",
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
          "x": 57.5,
          "y": 114
        }
      ],
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
      "id": "to-l-home-13",
      "color": "#3cc8e4",
      "name": "ROW_IN to L_HOME",
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
          "x": 57.5,
          "y": 114
        }
      ],
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
      "id": "to-flower-l-in-14",
      "color": "#3cc8e4",
      "name": "ROW_IN to FLOWER_L_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.36,
        "y": 121.79
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-flower-l-15",
      "color": "#3cc8e4",
      "name": "FLOWER_L_IN to FLOWER_L",
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
      "id": "to-flower-l-back-l-home-16",
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
      "id": "to-l-home-17",
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
      "id": "to-flower-l-turn-18",
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
      "id": "to-flower-l-19",
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
      "id": "to-flower-l-back-l-home-20",
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
      "id": "to-l-home-21",
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
      "id": "to-park-l-22",
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
      "lineId": "to-l-exit-1"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-2"
    },
    {
      "kind": "path",
      "lineId": "to-l-exit-back-3"
    },
    {
      "kind": "path",
      "lineId": "to-r-exit-back-4"
    },
    {
      "kind": "path",
      "lineId": "to-r-home-5"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-6"
    },
    {
      "kind": "path",
      "lineId": "to-garden-7"
    },
    {
      "kind": "path",
      "lineId": "to-r-back-8"
    },
    {
      "kind": "path",
      "lineId": "to-park-9"
    },
    {
      "kind": "path",
      "lineId": "to-park-10"
    },
    {
      "kind": "path",
      "lineId": "to-row-in-11"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-12"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-13"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-in-14"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-15"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-back-l-home-16"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-17"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-turn-18"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-19"
    },
    {
      "kind": "path",
      "lineId": "to-flower-l-back-l-home-20"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-21"
    },
    {
      "kind": "path",
      "lineId": "to-park-l-22"
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
    "exportName": "solo-tunnel",
    "registry": {
      "actions": [
        "LaunchAll",
        "CollectSeen"
      ],
      "conditions": [
        "Empty",
        "Tip",
        "IntakeFull",
        "LeftCellUp",
        "RightCellUp"
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
      "R_HOME": [
        57.5,
        10,
        90
      ],
      "L_HOME": [
        59,
        131.75,
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
      "ROW_IN": [
        34.6,
        112,
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
      "R_BACK": [
        59,
        11,
        90
      ],
      "PARK": [
        15,
        90,
        90
      ],
      "PARK_L": [
        15,
        89,
        270
      ],
      "FLOWER_L_BACK_L_HOME": [
        57.5,
        119.29,
        270
      ]
    },
    "pathEnds": {
      "to-l-exit-1": "L_EXIT",
      "to-l-home-2": "L_HOME",
      "to-l-exit-back-3": "L_EXIT_BACK",
      "to-r-exit-back-4": "R_EXIT_BACK",
      "to-r-home-5": "R_HOME",
      "to-garden-in-6": "GARDEN_IN",
      "to-garden-7": "GARDEN",
      "to-r-back-8": "R_BACK",
      "to-park-9": "PARK",
      "to-park-10": "PARK",
      "to-row-in-11": "ROW_IN",
      "to-l-home-12": "L_HOME",
      "to-l-home-13": "L_HOME",
      "to-flower-l-in-14": "FLOWER_L_IN",
      "to-flower-l-15": "FLOWER_L",
      "to-flower-l-back-l-home-16": "FLOWER_L_BACK_L_HOME",
      "to-l-home-17": "L_HOME",
      "to-flower-l-turn-18": "FLOWER_L_TURN",
      "to-flower-l-19": "FLOWER_L",
      "to-flower-l-back-l-home-20": "FLOWER_L_BACK_L_HOME",
      "to-l-home-21": "L_HOME",
      "to-park-l-22": "PARK_L"
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
        "id": "w-2",
        "kind": "firstOf",
        "label": "TIP 1: TIP?",
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
        ]
      },
      {
        "id": "w-3",
        "kind": "firstOf",
        "label": "TIP 1: catch the spill",
        "rows": [
          {
            "when": [
              "IntakeFull"
            ],
            "cards": []
          },
          {
            "afterMs": 1600,
            "cards": []
          }
        ]
      },
      {
        "id": "p-4",
        "kind": "path",
        "lineId": "to-l-exit-1",
        "park": false
      },
      {
        "id": "p-5",
        "kind": "path",
        "lineId": "to-l-home-2",
        "park": false
      },
      {
        "id": "w-6",
        "kind": "firstOf",
        "label": "Fire at the left CELL",
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
        "id": "w-40",
        "kind": "firstOf",
        "label": "TIP 2 yet?",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [
              {
                "id": "w-7",
                "kind": "firstOf",
                "label": "TIP 2: catch the spill",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1600,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-8",
                "kind": "path",
                "lineId": "to-l-exit-back-3",
                "park": false
              },
              {
                "id": "p-9",
                "kind": "path",
                "lineId": "to-r-exit-back-4",
                "park": false
              },
              {
                "id": "p-10",
                "kind": "path",
                "lineId": "to-r-home-5",
                "park": false
              },
              {
                "id": "w-11",
                "kind": "firstOf",
                "label": "Fire at the right CELL",
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
                "id": "w-19",
                "kind": "firstOf",
                "label": "TIP 3 yet?",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": [
                      {
                        "id": "p-18",
                        "kind": "path",
                        "lineId": "to-park-10",
                        "park": true
                      }
                    ],
                    "label": "Yes"
                  },
                  {
                    "afterMs": 1000,
                    "cards": [
                      {
                        "id": "p-12",
                        "kind": "path",
                        "lineId": "to-garden-in-6",
                        "park": false
                      },
                      {
                        "id": "p-13",
                        "kind": "path",
                        "lineId": "to-garden-7",
                        "park": false
                      },
                      {
                        "id": "w-14",
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
                        "id": "p-15",
                        "kind": "path",
                        "lineId": "to-r-back-8",
                        "park": false
                      },
                      {
                        "id": "w-16",
                        "kind": "firstOf",
                        "label": "Fire again (TIP 3)",
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
                        "id": "p-17",
                        "kind": "path",
                        "lineId": "to-park-9",
                        "park": true
                      }
                    ],
                    "label": "No: the GARDEN"
                  }
                ]
              }
            ],
            "label": "Yes: on to TIP 3"
          },
          {
            "afterMs": 2500,
            "cards": [
              {
                "id": "p-20",
                "kind": "path",
                "lineId": "to-row-in-11",
                "park": false
              },
              {
                "id": "w-29",
                "kind": "firstOf",
                "label": "Pick up the row",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": [
                      {
                        "id": "p-21",
                        "kind": "path",
                        "lineId": "to-l-home-12",
                        "park": false
                      }
                    ],
                    "label": "Full: home"
                  },
                  {
                    "afterMs": 2500,
                    "cards": [
                      {
                        "id": "w-28",
                        "kind": "firstOf",
                        "label": "Got any?",
                        "rows": [
                          {
                            "when": [
                              "Empty"
                            ],
                            "cards": [
                              {
                                "id": "p-23",
                                "kind": "path",
                                "lineId": "to-flower-l-in-14",
                                "park": false
                              },
                              {
                                "id": "p-24",
                                "kind": "path",
                                "lineId": "to-flower-l-15",
                                "park": false
                              },
                              {
                                "id": "w-25",
                                "kind": "firstOf",
                                "label": "Collect at the far FLOWER",
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
                                "id": "p-26",
                                "kind": "path",
                                "lineId": "to-flower-l-back-l-home-16",
                                "park": false
                              },
                              {
                                "id": "p-27",
                                "kind": "path",
                                "lineId": "to-l-home-17",
                                "park": false
                              }
                            ],
                            "label": "None: the far FLOWER"
                          },
                          {
                            "afterMs": 100,
                            "cards": [
                              {
                                "id": "p-22",
                                "kind": "path",
                                "lineId": "to-l-home-13",
                                "park": false
                              }
                            ],
                            "label": "Some: home"
                          }
                        ]
                      }
                    ],
                    "label": "Time up"
                  }
                ],
                "alongside": "CollectSeen"
              },
              {
                "id": "w-30",
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
                "id": "w-37",
                "kind": "firstOf",
                "label": "TIP 2 now?",
                "rows": [
                  {
                    "when": [
                      "RightCellUp"
                    ],
                    "cards": [],
                    "label": "Yes"
                  },
                  {
                    "afterMs": 100,
                    "cards": [
                      {
                        "id": "p-31",
                        "kind": "path",
                        "lineId": "to-flower-l-turn-18",
                        "park": false
                      },
                      {
                        "id": "p-32",
                        "kind": "path",
                        "lineId": "to-flower-l-19",
                        "park": false
                      },
                      {
                        "id": "w-33",
                        "kind": "firstOf",
                        "label": "Collect at the far FLOWER (2)",
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
                        "id": "p-34",
                        "kind": "path",
                        "lineId": "to-flower-l-back-l-home-20",
                        "park": false
                      },
                      {
                        "id": "p-35",
                        "kind": "path",
                        "lineId": "to-l-home-21",
                        "park": false
                      },
                      {
                        "id": "w-36",
                        "kind": "firstOf",
                        "label": "Fire once more (TIP 2)",
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
                      }
                    ],
                    "label": "No: the far FLOWER"
                  }
                ]
              },
              {
                "id": "w-38",
                "kind": "firstOf",
                "label": "TIP 2: catch the spill",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 1600,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-39",
                "kind": "path",
                "lineId": "to-park-l-22",
                "park": true
              }
            ],
            "label": "No: top up here"
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}