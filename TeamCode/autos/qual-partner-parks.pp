{
  "startPoint": {
    "x": 59,
    "y": 9.5,
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
      "id": "to-n-shoot-2",
      "color": "#3cc8e4",
      "name": "S_CATCH to N_SHOOT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 127.5
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.78,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 90
              }
            },
            {
              "startProgress": 0.78,
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
      "id": "to-row-in-3",
      "color": "#3cc8e4",
      "name": "N_SHOOT to ROW_IN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 34.6,
        "y": 114
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.7,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.7,
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
      "id": "to-row-back-4",
      "color": "#3cc8e4",
      "name": "ROW_IN to ROW_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 34.6,
        "y": 116
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-n-catch-5",
      "color": "#3cc8e4",
      "name": "ROW_BACK to N_CATCH",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 113.5
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.7,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
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
      "id": "to-s-catch-6",
      "color": "#3cc8e4",
      "name": "N_CATCH to S_CATCH",
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
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.9,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.9,
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
      "id": "to-garden-in-7",
      "color": "#3cc8e4",
      "name": "S_CATCH to GARDEN_IN",
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
      "id": "to-garden-8",
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
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-fire-garden-9",
      "color": "#3cc8e4",
      "name": "GARDEN to FIRE_GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14,
        "y": 24
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-wall-flower-turn-10",
      "color": "#3cc8e4",
      "name": "FIRE_GARDEN to WALL_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 22.21,
        "y": 47.36
      },
      "controlPoints": [
        {
          "x": 22.21,
          "y": 24
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
                "endDeg": 180
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-11",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER_TURN to WALL_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13.91,
        "y": 47.36
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-fire-wall-12",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER to FIRE_WALL",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 22.2,
        "y": 47.4
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-park-13",
      "color": "#3cc8e4",
      "name": "FIRE_WALL to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13,
        "y": 86
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 64
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-park-14",
      "color": "#3cc8e4",
      "name": "FIRE_GARDEN to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13,
        "y": 86
      },
      "controlPoints": [
        {
          "x": 34,
          "y": 36
        },
        {
          "x": 30,
          "y": 76
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-far-flower-in-15",
      "color": "#3cc8e4",
      "name": "ROW_BACK to FAR_FLOWER_IN",
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
      "id": "to-far-flower-16",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER_IN to FAR_FLOWER",
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
      "id": "to-fire-far-17",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to FIRE_FAR",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 47.4,
        "y": 119.3
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-n-catch-18",
      "color": "#3cc8e4",
      "name": "FIRE_FAR to N_CATCH",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 113.5
      },
      "controlPoints": [],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.7,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
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
      "id": "to-s-catch-19",
      "color": "#3cc8e4",
      "name": "N_CATCH to S_CATCH",
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
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.9,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.9,
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
      "id": "to-garden-in-20",
      "color": "#3cc8e4",
      "name": "S_CATCH to GARDEN_IN",
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
      "id": "to-garden-21",
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
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-fire-garden-22",
      "color": "#3cc8e4",
      "name": "GARDEN to FIRE_GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 14,
        "y": 24
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-wall-flower-turn-23",
      "color": "#3cc8e4",
      "name": "FIRE_GARDEN to WALL_FLOWER_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 22.21,
        "y": 47.36
      },
      "controlPoints": [
        {
          "x": 22.21,
          "y": 24
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
                "endDeg": 180
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-24",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER_TURN to WALL_FLOWER",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13.91,
        "y": 47.36
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-fire-wall-25",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER to FIRE_WALL",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 22.2,
        "y": 47.4
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-park-26",
      "color": "#3cc8e4",
      "name": "FIRE_WALL to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13,
        "y": 86
      },
      "controlPoints": [
        {
          "x": 26,
          "y": 64
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-park-27",
      "color": "#3cc8e4",
      "name": "FIRE_GARDEN to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13,
        "y": 86
      },
      "controlPoints": [
        {
          "x": 34,
          "y": 36
        },
        {
          "x": 30,
          "y": 76
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
      "lineId": "to-s-catch-1"
    },
    {
      "kind": "path",
      "lineId": "to-n-shoot-2"
    },
    {
      "kind": "path",
      "lineId": "to-row-in-3"
    },
    {
      "kind": "path",
      "lineId": "to-row-back-4"
    },
    {
      "kind": "path",
      "lineId": "to-n-catch-5"
    },
    {
      "kind": "path",
      "lineId": "to-s-catch-6"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-7"
    },
    {
      "kind": "path",
      "lineId": "to-garden-8"
    },
    {
      "kind": "path",
      "lineId": "to-fire-garden-9"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-turn-10"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-11"
    },
    {
      "kind": "path",
      "lineId": "to-fire-wall-12"
    },
    {
      "kind": "path",
      "lineId": "to-park-13"
    },
    {
      "kind": "path",
      "lineId": "to-park-14"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-in-15"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-16"
    },
    {
      "kind": "path",
      "lineId": "to-fire-far-17"
    },
    {
      "kind": "path",
      "lineId": "to-n-catch-18"
    },
    {
      "kind": "path",
      "lineId": "to-s-catch-19"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-20"
    },
    {
      "kind": "path",
      "lineId": "to-garden-21"
    },
    {
      "kind": "path",
      "lineId": "to-fire-garden-22"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-turn-23"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-24"
    },
    {
      "kind": "path",
      "lineId": "to-fire-wall-25"
    },
    {
      "kind": "path",
      "lineId": "to-park-26"
    },
    {
      "kind": "path",
      "lineId": "to-park-27"
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
    "exportName": "qual-partner-parks",
    "registry": {
      "actions": [
        "LaunchAll",
        "CollectSeen"
      ],
      "conditions": [
        "Empty",
        "LeftCellUp",
        "IntakeFull",
        "Tip",
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
        22,
        270
      ],
      "GARDEN": [
        8.5,
        11,
        270
      ],
      "PARK": [
        13,
        86,
        90
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
      "FIRE_GARDEN": [
        14,
        24,
        270
      ],
      "FIRE_WALL": [
        22.2,
        47.4,
        180
      ],
      "FIRE_FAR": [
        47.4,
        119.3,
        90
      ],
      "ROW_IN": [
        34.6,
        114,
        90
      ],
      "ROW_BACK": [
        34.6,
        116,
        90
      ]
    },
    "pathEnds": {
      "to-s-catch-1": "S_CATCH",
      "to-n-shoot-2": "N_SHOOT",
      "to-row-in-3": "ROW_IN",
      "to-row-back-4": "ROW_BACK",
      "to-n-catch-5": "N_CATCH",
      "to-s-catch-6": "S_CATCH",
      "to-garden-in-7": "GARDEN_IN",
      "to-garden-8": "GARDEN",
      "to-fire-garden-9": "FIRE_GARDEN",
      "to-wall-flower-turn-10": "WALL_FLOWER_TURN",
      "to-wall-flower-11": "WALL_FLOWER",
      "to-fire-wall-12": "FIRE_WALL",
      "to-park-13": "PARK",
      "to-park-14": "PARK",
      "to-far-flower-in-15": "FAR_FLOWER_IN",
      "to-far-flower-16": "FAR_FLOWER",
      "to-fire-far-17": "FIRE_FAR",
      "to-n-catch-18": "N_CATCH",
      "to-s-catch-19": "S_CATCH",
      "to-garden-in-20": "GARDEN_IN",
      "to-garden-21": "GARDEN",
      "to-fire-garden-22": "FIRE_GARDEN",
      "to-wall-flower-turn-23": "WALL_FLOWER_TURN",
      "to-wall-flower-24": "WALL_FLOWER",
      "to-fire-wall-25": "FIRE_WALL",
      "to-park-26": "PARK",
      "to-park-27": "PARK"
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
        "id": "p-2",
        "kind": "path",
        "lineId": "to-s-catch-1",
        "park": false
      },
      {
        "id": "w-3",
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
        "id": "p-4",
        "kind": "path",
        "lineId": "to-n-shoot-2",
        "park": false
      },
      {
        "id": "w-5",
        "kind": "firstOf",
        "label": "Fire the catch at the left CELL",
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
        "id": "p-6",
        "kind": "path",
        "lineId": "to-row-in-3",
        "park": false
      },
      {
        "id": "w-7",
        "kind": "firstOf",
        "label": "The partner's row",
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
        "id": "p-8",
        "kind": "path",
        "lineId": "to-row-back-4",
        "park": false
      },
      {
        "id": "w-9",
        "kind": "firstOf",
        "label": "Fire the row (TIP 2)",
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
        "id": "w-49",
        "kind": "firstOf",
        "label": "TIP 2?",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": [
              {
                "id": "p-10",
                "kind": "path",
                "lineId": "to-n-catch-5",
                "park": false
              },
              {
                "id": "w-11",
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
                "id": "p-12",
                "kind": "path",
                "lineId": "to-s-catch-6",
                "park": false
              },
              {
                "id": "w-13",
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
                "id": "p-14",
                "kind": "path",
                "lineId": "to-garden-in-7",
                "park": false
              },
              {
                "id": "p-15",
                "kind": "path",
                "lineId": "to-garden-8",
                "park": false
              },
              {
                "id": "w-16",
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
                "id": "p-17",
                "kind": "path",
                "lineId": "to-fire-garden-9",
                "park": false
              },
              {
                "id": "w-18",
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
                    "afterMs": 2500,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              },
              {
                "id": "w-26",
                "kind": "firstOf",
                "label": "TIP 3?",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": [
                      {
                        "id": "p-25",
                        "kind": "path",
                        "lineId": "to-park-14",
                        "park": true
                      }
                    ],
                    "label": "Yes: PARK"
                  },
                  {
                    "afterMs": 600,
                    "cards": [
                      {
                        "id": "p-19",
                        "kind": "path",
                        "lineId": "to-wall-flower-turn-10",
                        "park": false
                      },
                      {
                        "id": "p-20",
                        "kind": "path",
                        "lineId": "to-wall-flower-11",
                        "park": false
                      },
                      {
                        "id": "w-21",
                        "kind": "firstOf",
                        "label": "The wall FLOWER",
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
                        "id": "p-22",
                        "kind": "path",
                        "lineId": "to-fire-wall-12",
                        "park": false
                      },
                      {
                        "id": "w-23",
                        "kind": "firstOf",
                        "label": "Fire the wall FLOWER (TIP 3)",
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
                        "id": "p-24",
                        "kind": "path",
                        "lineId": "to-park-13",
                        "park": false
                      }
                    ],
                    "label": "No: the wall FLOWER"
                  }
                ]
              }
            ],
            "label": "Yes"
          },
          {
            "afterMs": 300,
            "cards": [
              {
                "id": "p-27",
                "kind": "path",
                "lineId": "to-far-flower-in-15",
                "park": false
              },
              {
                "id": "p-28",
                "kind": "path",
                "lineId": "to-far-flower-16",
                "park": false
              },
              {
                "id": "w-29",
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
                "id": "p-30",
                "kind": "path",
                "lineId": "to-fire-far-17",
                "park": false
              },
              {
                "id": "w-31",
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
                "id": "p-32",
                "kind": "path",
                "lineId": "to-n-catch-18",
                "park": false
              },
              {
                "id": "w-33",
                "kind": "firstOf",
                "label": "TIP 2 settles (B)",
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
                "id": "p-34",
                "kind": "path",
                "lineId": "to-s-catch-19",
                "park": false
              },
              {
                "id": "w-35",
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
                "id": "p-36",
                "kind": "path",
                "lineId": "to-garden-in-20",
                "park": false
              },
              {
                "id": "p-37",
                "kind": "path",
                "lineId": "to-garden-21",
                "park": false
              },
              {
                "id": "w-38",
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
                "id": "p-39",
                "kind": "path",
                "lineId": "to-fire-garden-22",
                "park": false
              },
              {
                "id": "w-40",
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
                    "afterMs": 2500,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              },
              {
                "id": "w-48",
                "kind": "firstOf",
                "label": "TIP 3? (B)",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": [
                      {
                        "id": "p-47",
                        "kind": "path",
                        "lineId": "to-park-27",
                        "park": true
                      }
                    ],
                    "label": "Yes: PARK"
                  },
                  {
                    "afterMs": 600,
                    "cards": [
                      {
                        "id": "p-41",
                        "kind": "path",
                        "lineId": "to-wall-flower-turn-23",
                        "park": false
                      },
                      {
                        "id": "p-42",
                        "kind": "path",
                        "lineId": "to-wall-flower-24",
                        "park": false
                      },
                      {
                        "id": "w-43",
                        "kind": "firstOf",
                        "label": "The wall FLOWER (B)",
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
                        "id": "p-44",
                        "kind": "path",
                        "lineId": "to-fire-wall-25",
                        "park": false
                      },
                      {
                        "id": "w-45",
                        "kind": "firstOf",
                        "label": "Fire the wall FLOWER (TIP 3) (B)",
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
                        "id": "p-46",
                        "kind": "path",
                        "lineId": "to-park-26",
                        "park": false
                      }
                    ],
                    "label": "No: the wall FLOWER"
                  }
                ]
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