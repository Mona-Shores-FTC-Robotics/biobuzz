{
  "startPoint": {
    "x": 59,
    "y": 132.25,
    "name": "START",
    "headingDeg": 270
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
        "x": 57.5,
        "y": 108
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-r-exit-2",
      "color": "#3cc8e4",
      "name": "L_EXIT to R_EXIT",
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
      "id": "to-r-home-3",
      "color": "#3cc8e4",
      "name": "R_EXIT to R_HOME",
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
      "id": "to-l-exit-4",
      "color": "#3cc8e4",
      "name": "R_HOME to L_EXIT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 108
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-l-home-5",
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
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      }
    },
    {
      "id": "to-far-flower-turn-6",
      "color": "#3cc8e4",
      "name": "L_HOME to FAR_FLOWER_TURN",
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
      "id": "to-far-flower-7",
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
      "id": "to-far-flower-back-l-home-8",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to FAR_FLOWER_BACK_L_HOME",
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
      "id": "to-l-home-9",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER_BACK_L_HOME to L_HOME",
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
      "id": "to-park-10",
      "color": "#3cc8e4",
      "name": "L_HOME to PARK",
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
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0,
              "endProgress": 0.45,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.45,
              "endProgress": 0.9,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 270,
                "endDeg": 90
              }
            },
            {
              "startProgress": 0.9,
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
      "id": "to-park2-11",
      "color": "#3cc8e4",
      "name": "PARK to PARK2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 15,
        "y": 90
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 90
      }
    },
    {
      "id": "to-far-flower-turn-12",
      "color": "#3cc8e4",
      "name": "START to FAR_FLOWER_TURN",
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
      "id": "to-far-flower-13",
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
      "id": "to-far-flower-back-l-home-14",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to FAR_FLOWER_BACK_L_HOME",
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
      "id": "to-l-home-15",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER_BACK_L_HOME to L_HOME",
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
      "id": "to-l-exit-16",
      "color": "#3cc8e4",
      "name": "L_HOME to L_EXIT",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 108
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-r-exit-17",
      "color": "#3cc8e4",
      "name": "L_EXIT to R_EXIT",
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
      "id": "to-right-shot-18",
      "color": "#3cc8e4",
      "name": "R_EXIT to RIGHT_SHOT",
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
      "id": "to-right-plunge-in-19",
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
      "id": "to-right-plunge-20",
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
      "id": "to-right-shot-21",
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
      "id": "to-garden-22",
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
      "id": "to-right-shot-23",
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
      "id": "to-park-24",
      "color": "#3cc8e4",
      "name": "RIGHT_SHOT to PARK",
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
          "x": 24,
          "y": 40
        },
        {
          "x": 24,
          "y": 80
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-park2-25",
      "color": "#3cc8e4",
      "name": "PARK to PARK2",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 15,
        "y": 90
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
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
      "lineId": "to-l-exit-1"
    },
    {
      "kind": "path",
      "lineId": "to-r-exit-2"
    },
    {
      "kind": "path",
      "lineId": "to-r-home-3"
    },
    {
      "kind": "path",
      "lineId": "to-l-exit-4"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-5"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-turn-6"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-7"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-back-l-home-8"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-9"
    },
    {
      "kind": "path",
      "lineId": "to-park-10"
    },
    {
      "kind": "path",
      "lineId": "to-park2-11"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-turn-12"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-13"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-back-l-home-14"
    },
    {
      "kind": "path",
      "lineId": "to-l-home-15"
    },
    {
      "kind": "path",
      "lineId": "to-l-exit-16"
    },
    {
      "kind": "path",
      "lineId": "to-r-exit-17"
    },
    {
      "kind": "path",
      "lineId": "to-right-shot-18"
    },
    {
      "kind": "path",
      "lineId": "to-right-plunge-in-19"
    },
    {
      "kind": "path",
      "lineId": "to-right-plunge-20"
    },
    {
      "kind": "path",
      "lineId": "to-right-shot-21"
    },
    {
      "kind": "path",
      "lineId": "to-garden-22"
    },
    {
      "kind": "path",
      "lineId": "to-right-shot-23"
    },
    {
      "kind": "path",
      "lineId": "to-park-24"
    },
    {
      "kind": "path",
      "lineId": "to-park2-25"
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
    "exportName": "left-first",
    "registry": {
      "actions": [
        "LaunchAll",
        "SpinUp"
      ],
      "conditions": [
        "LeftCellUp",
        "IntakeFull",
        "Empty",
        "RightCellUp"
      ],
      "typicalS": {
        "LaunchAll": 2.0,
        "SpinUp": 0.1
      },
      "events": []
    },
    "points": {
      "START": [
        59,
        132.25,
        270
      ],
      "L_HOME": [
        59,
        131.75,
        270
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
      "L_EXIT": [
        57.5,
        108,
        270
      ],
      "R_EXIT": [
        57.5,
        34,
        270
      ],
      "R_HOME": [
        57.5,
        10,
        90
      ],
      "RIGHT_SHOT": [
        36,
        30,
        49
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
      "GARDEN": [
        8.5,
        11,
        270
      ],
      "PARK": [
        15,
        89,
        90
      ],
      "PARK2": [
        15,
        90,
        90
      ],
      "FAR_FLOWER_BACK_L_HOME": [
        57.5,
        119.29,
        270
      ]
    },
    "pathEnds": {
      "to-l-exit-1": "L_EXIT",
      "to-r-exit-2": "R_EXIT",
      "to-r-home-3": "R_HOME",
      "to-l-exit-4": "L_EXIT",
      "to-l-home-5": "L_HOME",
      "to-far-flower-turn-6": "FAR_FLOWER_TURN",
      "to-far-flower-7": "FAR_FLOWER",
      "to-far-flower-back-l-home-8": "FAR_FLOWER_BACK_L_HOME",
      "to-l-home-9": "L_HOME",
      "to-park-10": "PARK",
      "to-park2-11": "PARK2",
      "to-far-flower-turn-12": "FAR_FLOWER_TURN",
      "to-far-flower-13": "FAR_FLOWER",
      "to-far-flower-back-l-home-14": "FAR_FLOWER_BACK_L_HOME",
      "to-l-home-15": "L_HOME",
      "to-l-exit-16": "L_EXIT",
      "to-r-exit-17": "R_EXIT",
      "to-right-shot-18": "RIGHT_SHOT",
      "to-right-plunge-in-19": "RIGHT_PLUNGE_IN",
      "to-right-plunge-20": "RIGHT_PLUNGE",
      "to-right-shot-21": "RIGHT_SHOT",
      "to-garden-22": "GARDEN",
      "to-right-shot-23": "RIGHT_SHOT",
      "to-park-24": "PARK",
      "to-park2-25": "PARK2"
    },
    "startAt": "START",
    "cards": [
      {
        "id": "a-41",
        "kind": "action",
        "name": "SpinUp"
      },
      {
        "id": "w-42",
        "kind": "firstOf",
        "label": "Left CELL up (partner's TIP 1)?",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": [
              {
                "id": "w-17",
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
                    "afterMs": 2000,
                    "cards": []
                  }
                ],
                "alongside": "LaunchAll"
              },
              {
                "id": "p-18",
                "kind": "path",
                "lineId": "to-far-flower-turn-12",
                "park": false
              },
              {
                "id": "p-19",
                "kind": "path",
                "lineId": "to-far-flower-13",
                "park": false
              },
              {
                "id": "w-20",
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
                    "afterMs": 2300,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-21",
                "kind": "path",
                "lineId": "to-far-flower-back-l-home-14",
                "park": false
              },
              {
                "id": "p-22",
                "kind": "path",
                "lineId": "to-l-home-15",
                "park": false
              },
              {
                "id": "w-23",
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
                "id": "w-24",
                "kind": "firstOf",
                "label": "Catch the TIP 2 spill",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 2200,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-25",
                "kind": "path",
                "lineId": "to-l-exit-16",
                "park": false
              },
              {
                "id": "p-26",
                "kind": "path",
                "lineId": "to-r-exit-17",
                "park": false
              },
              {
                "id": "p-27",
                "kind": "path",
                "lineId": "to-right-shot-18",
                "park": false
              },
              {
                "id": "a-28",
                "kind": "action",
                "name": "LaunchAll"
              },
              {
                "id": "p-29",
                "kind": "path",
                "lineId": "to-right-plunge-in-19",
                "park": false
              },
              {
                "id": "p-30",
                "kind": "path",
                "lineId": "to-right-plunge-20",
                "park": false
              },
              {
                "id": "w-31",
                "kind": "firstOf",
                "label": "The TIP 1 spill at the wall",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 900,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-32",
                "kind": "path",
                "lineId": "to-right-shot-21",
                "park": false
              },
              {
                "id": "w-33",
                "kind": "firstOf",
                "label": "Fire (TIP 3)",
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
                "id": "w-38",
                "kind": "firstOf",
                "label": "TIP 3 yet?",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": [],
                    "label": "Yes"
                  },
                  {
                    "afterMs": 1500,
                    "cards": [
                      {
                        "id": "p-34",
                        "kind": "path",
                        "lineId": "to-garden-22",
                        "park": false
                      },
                      {
                        "id": "w-35",
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
                        "id": "p-36",
                        "kind": "path",
                        "lineId": "to-right-shot-23",
                        "park": false
                      },
                      {
                        "id": "w-37",
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
                    "label": "No: the GARDEN"
                  }
                ]
              },
              {
                "id": "p-39",
                "kind": "path",
                "lineId": "to-park-24",
                "park": false
              },
              {
                "id": "p-40",
                "kind": "path",
                "lineId": "to-park2-25",
                "park": true
              }
            ],
            "label": "Yes"
          },
          {
            "afterMs": 6000,
            "cards": [
              {
                "id": "p-1",
                "kind": "path",
                "lineId": "to-l-exit-1",
                "park": false
              },
              {
                "id": "p-2",
                "kind": "path",
                "lineId": "to-r-exit-2",
                "park": false
              },
              {
                "id": "p-3",
                "kind": "path",
                "lineId": "to-r-home-3",
                "park": false
              },
              {
                "id": "w-4",
                "kind": "firstOf",
                "label": "Fire until it tips (our TIP 1)",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
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
                "id": "w-5",
                "kind": "firstOf",
                "label": "Catch the TIP 1 spill",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 2200,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-6",
                "kind": "path",
                "lineId": "to-l-exit-4",
                "park": false
              },
              {
                "id": "p-7",
                "kind": "path",
                "lineId": "to-l-home-5",
                "park": false
              },
              {
                "id": "w-8",
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
                "id": "p-9",
                "kind": "path",
                "lineId": "to-far-flower-turn-6",
                "park": false
              },
              {
                "id": "p-10",
                "kind": "path",
                "lineId": "to-far-flower-7",
                "park": false
              },
              {
                "id": "w-11",
                "kind": "firstOf",
                "label": "Collect at the FLOWER (fallback)",
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
                "id": "p-12",
                "kind": "path",
                "lineId": "to-far-flower-back-l-home-8",
                "park": false
              },
              {
                "id": "p-13",
                "kind": "path",
                "lineId": "to-l-home-9",
                "park": false
              },
              {
                "id": "w-14",
                "kind": "firstOf",
                "label": "Fire until it tips (TIP 2, fallback)",
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
                "id": "p-15",
                "kind": "path",
                "lineId": "to-park-10",
                "park": false
              },
              {
                "id": "p-16",
                "kind": "path",
                "lineId": "to-park2-11",
                "park": true
              }
            ],
            "label": "No: the partner missed"
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}