{
  "startPoint": {
    "x": 59.0,
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
      "id": "to-n-turn-2",
      "color": "#3cc8e4",
      "name": "S_CATCH to N_TURN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
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
      "id": "to-s-fire-4",
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
        "y": 10
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
        "y": 10
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
        "x": 8.5,
        "y": 11.0
      },
      "controlPoints": [
        {
          "x": 8.5,
          "y": 14
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
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
        "x": 13.0,
        "y": 86.0
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
    },
    {
      "id": "to-garden-10",
      "color": "#3cc8e4",
      "name": "S_FIRE to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 11.0
      },
      "controlPoints": [
        {
          "x": 8.5,
          "y": 30
        }
      ],
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
      "id": "to-park-11",
      "color": "#3cc8e4",
      "name": "GARDEN to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13.0,
        "y": 86.0
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 22
        },
        {
          "x": 26,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-s-fire-12",
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
      "id": "to-park-13",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13.0,
        "y": 86.0
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
    },
    {
      "id": "to-park-14",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13.0,
        "y": 86.0
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
    },
    {
      "id": "to-far-flower-turn-15",
      "color": "#3cc8e4",
      "name": "N_LOW to FAR_FLOWER_TURN",
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
          "x": 57.5,
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
      "id": "to-far-flower-16",
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
      "id": "to-n-fire-17",
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
      "id": "to-s-fire-18",
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
      "id": "to-sweep-e-19",
      "color": "#3cc8e4",
      "name": "S_FIRE to SWEEP_E",
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
        "type": "linear",
        "startDeg": 90,
        "endDeg": 180
      }
    },
    {
      "id": "to-sweep-w-20",
      "color": "#3cc8e4",
      "name": "SWEEP_E to SWEEP_W",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 22,
        "y": 10
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 180
      }
    },
    {
      "id": "to-garden-21",
      "color": "#3cc8e4",
      "name": "SWEEP_W to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 11.0
      },
      "controlPoints": [
        {
          "x": 8.5,
          "y": 14
        }
      ],
      "heading": {
        "type": "piecewise",
        "piecewiseHeading": {
          "segments": [
            {
              "startProgress": 0.0,
              "endProgress": 0.8,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
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
      "id": "to-s-fire-22",
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
      "id": "to-park-23",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13.0,
        "y": 86.0
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
    },
    {
      "id": "to-garden-24",
      "color": "#3cc8e4",
      "name": "S_FIRE to GARDEN",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 8.5,
        "y": 11.0
      },
      "controlPoints": [
        {
          "x": 8.5,
          "y": 30
        }
      ],
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
      "id": "to-park-25",
      "color": "#3cc8e4",
      "name": "GARDEN to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13.0,
        "y": 86.0
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 22
        },
        {
          "x": 26,
          "y": 70
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-s-fire-26",
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
      "id": "to-park-27",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13.0,
        "y": 86.0
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
    },
    {
      "id": "to-park-28",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 13.0,
        "y": 86.0
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
    },
    {
      "kind": "path",
      "lineId": "to-garden-10"
    },
    {
      "kind": "path",
      "lineId": "to-park-11"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-12"
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
      "lineId": "to-far-flower-turn-15"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-16"
    },
    {
      "kind": "path",
      "lineId": "to-n-fire-17"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-18"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-e-19"
    },
    {
      "kind": "path",
      "lineId": "to-sweep-w-20"
    },
    {
      "kind": "path",
      "lineId": "to-garden-21"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-22"
    },
    {
      "kind": "path",
      "lineId": "to-park-23"
    },
    {
      "kind": "path",
      "lineId": "to-garden-24"
    },
    {
      "kind": "path",
      "lineId": "to-park-25"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-26"
    },
    {
      "kind": "path",
      "lineId": "to-park-27"
    },
    {
      "kind": "path",
      "lineId": "to-park-28"
    }
  ],
  "settings": {
    "xVelocity": 75,
    "yVelocity": 65,
    "aVelocity": 3.141592653589793,
    "kFriction": 0.1,
    "rWidth": 18.0,
    "rHeight": 18.0,
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
    "exportName": "qual-left-wide",
    "registry": {
      "actions": [
        "LaunchAll"
      ],
      "conditions": [
        "Empty",
        "LeftCellUp",
        "IntakeFull",
        "RightCellUp",
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
        22.0,
        270
      ],
      "GARDEN": [
        8.5,
        11.0,
        270
      ],
      "PARK": [
        13.0,
        86.0,
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
      "SWEEP_E": [
        57.5,
        10,
        180
      ],
      "SWEEP_W": [
        22,
        10,
        180
      ]
    },
    "pathEnds": {
      "to-s-catch-1": "S_CATCH",
      "to-n-turn-2": "N_TURN",
      "to-n-low-3": "N_LOW",
      "to-s-fire-4": "S_FIRE",
      "to-sweep-e-5": "SWEEP_E",
      "to-sweep-w-6": "SWEEP_W",
      "to-garden-7": "GARDEN",
      "to-s-fire-8": "S_FIRE",
      "to-park-9": "PARK",
      "to-garden-10": "GARDEN",
      "to-park-11": "PARK",
      "to-s-fire-12": "S_FIRE",
      "to-park-13": "PARK",
      "to-park-14": "PARK",
      "to-far-flower-turn-15": "FAR_FLOWER_TURN",
      "to-far-flower-16": "FAR_FLOWER",
      "to-n-fire-17": "N_FIRE",
      "to-s-fire-18": "S_FIRE",
      "to-sweep-e-19": "SWEEP_E",
      "to-sweep-w-20": "SWEEP_W",
      "to-garden-21": "GARDEN",
      "to-s-fire-22": "S_FIRE",
      "to-park-23": "PARK",
      "to-garden-24": "GARDEN",
      "to-park-25": "PARK",
      "to-s-fire-26": "S_FIRE",
      "to-park-27": "PARK",
      "to-park-28": "PARK"
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
        "id": "w-4",
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
        "id": "w-55",
        "kind": "firstOf",
        "label": "TIP 2?",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": [
              {
                "id": "w-8",
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
                "id": "w-9",
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
                "id": "p-10",
                "kind": "path",
                "lineId": "to-s-fire-4",
                "park": false
              },
              {
                "id": "w-11",
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
                "id": "p-12",
                "kind": "path",
                "lineId": "to-sweep-e-5",
                "park": false
              },
              {
                "id": "p-13",
                "kind": "path",
                "lineId": "to-sweep-w-6",
                "park": false
              },
              {
                "id": "p-14",
                "kind": "path",
                "lineId": "to-garden-7",
                "park": false
              },
              {
                "id": "w-15",
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
                "id": "p-16",
                "kind": "path",
                "lineId": "to-s-fire-8",
                "park": false
              },
              {
                "id": "w-17",
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
                "id": "w-28",
                "kind": "firstOf",
                "label": "TIP 3 coming?",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": [
                      {
                        "id": "p-27",
                        "kind": "path",
                        "lineId": "to-park-14",
                        "park": true
                      }
                    ],
                    "label": "TIP 3: PARK"
                  },
                  {
                    "afterMs": 800,
                    "cards": [
                      {
                        "id": "w-26",
                        "kind": "firstOf",
                        "label": "No TIP 3 yet?",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": [
                              {
                                "id": "p-19",
                                "kind": "path",
                                "lineId": "to-garden-10",
                                "park": false
                              },
                              {
                                "id": "w-25",
                                "kind": "firstOf",
                                "label": "Still no TIP 3?",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": [
                                      {
                                        "id": "w-21",
                                        "kind": "firstOf",
                                        "label": "The GARDEN again",
                                        "rows": [
                                          {
                                            "when": [
                                              "IntakeFull"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1100,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "p-22",
                                        "kind": "path",
                                        "lineId": "to-s-fire-12",
                                        "park": false
                                      },
                                      {
                                        "id": "w-23",
                                        "kind": "firstOf",
                                        "label": "Fire the GARDEN again",
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
                                    "label": "No TIP: fire the GARDEN"
                                  },
                                  {
                                    "afterMs": 20,
                                    "cards": [
                                      {
                                        "id": "p-20",
                                        "kind": "path",
                                        "lineId": "to-park-11",
                                        "park": true
                                      }
                                    ],
                                    "label": "TIP 3: PARK"
                                  }
                                ]
                              }
                            ],
                            "label": "No TIP: the GARDEN"
                          },
                          {
                            "afterMs": 20,
                            "cards": [
                              {
                                "id": "p-18",
                                "kind": "path",
                                "lineId": "to-park-9",
                                "park": true
                              }
                            ],
                            "label": "TIP 3: PARK"
                          }
                        ]
                      }
                    ],
                    "label": "Not yet: look again"
                  }
                ]
              }
            ],
            "label": "Yes"
          },
          {
            "afterMs": 1000,
            "cards": [
              {
                "id": "p-29",
                "kind": "path",
                "lineId": "to-far-flower-turn-15",
                "park": false
              },
              {
                "id": "p-30",
                "kind": "path",
                "lineId": "to-far-flower-16",
                "park": false
              },
              {
                "id": "w-31",
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
                "id": "p-32",
                "kind": "path",
                "lineId": "to-n-fire-17",
                "park": false
              },
              {
                "id": "w-33",
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
                "id": "w-34",
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
                "id": "w-35",
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
                    "afterMs": 500,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-36",
                "kind": "path",
                "lineId": "to-s-fire-18",
                "park": false
              },
              {
                "id": "w-37",
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
                "id": "p-38",
                "kind": "path",
                "lineId": "to-sweep-e-19",
                "park": false
              },
              {
                "id": "p-39",
                "kind": "path",
                "lineId": "to-sweep-w-20",
                "park": false
              },
              {
                "id": "p-40",
                "kind": "path",
                "lineId": "to-garden-21",
                "park": false
              },
              {
                "id": "w-41",
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
                "id": "p-42",
                "kind": "path",
                "lineId": "to-s-fire-22",
                "park": false
              },
              {
                "id": "w-43",
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
              },
              {
                "id": "w-54",
                "kind": "firstOf",
                "label": "TIP 3 coming? (B)",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": [
                      {
                        "id": "p-53",
                        "kind": "path",
                        "lineId": "to-park-28",
                        "park": true
                      }
                    ],
                    "label": "TIP 3: PARK"
                  },
                  {
                    "afterMs": 800,
                    "cards": [
                      {
                        "id": "w-52",
                        "kind": "firstOf",
                        "label": "No TIP 3 yet? (B)",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": [
                              {
                                "id": "p-45",
                                "kind": "path",
                                "lineId": "to-garden-24",
                                "park": false
                              },
                              {
                                "id": "w-51",
                                "kind": "firstOf",
                                "label": "Still no TIP 3? (B)",
                                "rows": [
                                  {
                                    "when": [
                                      "RightCellUp"
                                    ],
                                    "cards": [
                                      {
                                        "id": "w-47",
                                        "kind": "firstOf",
                                        "label": "The GARDEN again (B)",
                                        "rows": [
                                          {
                                            "when": [
                                              "IntakeFull"
                                            ],
                                            "cards": []
                                          },
                                          {
                                            "afterMs": 1100,
                                            "cards": []
                                          }
                                        ]
                                      },
                                      {
                                        "id": "p-48",
                                        "kind": "path",
                                        "lineId": "to-s-fire-26",
                                        "park": false
                                      },
                                      {
                                        "id": "w-49",
                                        "kind": "firstOf",
                                        "label": "Fire the GARDEN again (B)",
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
                                        "id": "p-50",
                                        "kind": "path",
                                        "lineId": "to-park-27",
                                        "park": false
                                      }
                                    ],
                                    "label": "No TIP: fire the GARDEN"
                                  },
                                  {
                                    "afterMs": 20,
                                    "cards": [
                                      {
                                        "id": "p-46",
                                        "kind": "path",
                                        "lineId": "to-park-25",
                                        "park": true
                                      }
                                    ],
                                    "label": "TIP 3: PARK"
                                  }
                                ]
                              }
                            ],
                            "label": "No TIP: the GARDEN"
                          },
                          {
                            "afterMs": 20,
                            "cards": [
                              {
                                "id": "p-44",
                                "kind": "path",
                                "lineId": "to-park-23",
                                "park": true
                              }
                            ],
                            "label": "TIP 3: PARK"
                          }
                        ]
                      }
                    ],
                    "label": "Not yet: look again"
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