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
      "id": "to-n-pick-2",
      "color": "#3cc8e4",
      "name": "S_CATCH to N_PICK",
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
      "id": "to-n-low-3",
      "color": "#3cc8e4",
      "name": "N_PICK to N_LOW",
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
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    },
    {
      "id": "to-n-catch-4",
      "color": "#3cc8e4",
      "name": "N_LOW to N_CATCH",
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
        "type": "linear",
        "startDeg": 270,
        "endDeg": 270
      }
    },
    {
      "id": "to-n-back-5",
      "color": "#3cc8e4",
      "name": "N_CATCH to N_BACK",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 57.5,
        "y": 116.5
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
      "name": "N_BACK to FAR_FLOWER_TURN",
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
      "id": "to-n-fire-8",
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
      "id": "to-s-catch-9",
      "color": "#3cc8e4",
      "name": "N_FIRE to S_CATCH",
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
      "id": "to-garden-in-10",
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
      "id": "to-garden-11",
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
      "id": "to-wall-flower-turn-13",
      "color": "#3cc8e4",
      "name": "S_FIRE to WALL_FLOWER_TURN",
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
                "degrees": 90
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 180
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-14",
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
      "id": "to-s-fire-15",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER to S_FIRE",
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
                "degrees": 180
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-16",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
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
      "id": "to-park-17",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
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
      "id": "to-s-catch-18",
      "color": "#3cc8e4",
      "name": "N_BACK to S_CATCH",
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
              "endProgress": 0.91,
              "interpolationType": "constant",
              "parameters": {
                "degrees": 270
              }
            },
            {
              "startProgress": 0.91,
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
        "y": 11
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
    },
    {
      "id": "to-wall-flower-turn-22",
      "color": "#3cc8e4",
      "name": "S_FIRE to WALL_FLOWER_TURN",
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
                "degrees": 90
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 180
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-23",
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
      "id": "to-s-fire-24",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER to S_FIRE",
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
                "degrees": 180
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-25",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
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
      "id": "to-park-26",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
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
      "id": "to-s-catch-27",
      "color": "#3cc8e4",
      "name": "N_LOW to S_CATCH",
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
      "id": "to-garden-in-28",
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
      "id": "to-garden-29",
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
      "id": "to-s-fire-30",
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
      "id": "to-wall-flower-turn-31",
      "color": "#3cc8e4",
      "name": "S_FIRE to WALL_FLOWER_TURN",
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
                "degrees": 90
              }
            },
            {
              "startProgress": 0.5,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 90,
                "endDeg": 180
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-wall-flower-32",
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
      "id": "to-s-fire-33",
      "color": "#3cc8e4",
      "name": "WALL_FLOWER to S_FIRE",
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
                "degrees": 180
              }
            },
            {
              "startProgress": 0.3,
              "endProgress": 1.0,
              "interpolationType": "linear",
              "parameters": {
                "startDeg": 180,
                "endDeg": 90
              }
            }
          ]
        }
      }
    },
    {
      "id": "to-park-34",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
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
      "id": "to-park-35",
      "color": "#3cc8e4",
      "name": "S_FIRE to PARK",
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
      "lineId": "to-n-pick-2"
    },
    {
      "kind": "path",
      "lineId": "to-n-low-3"
    },
    {
      "kind": "path",
      "lineId": "to-n-catch-4"
    },
    {
      "kind": "path",
      "lineId": "to-n-back-5"
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
      "lineId": "to-n-fire-8"
    },
    {
      "kind": "path",
      "lineId": "to-s-catch-9"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-10"
    },
    {
      "kind": "path",
      "lineId": "to-garden-11"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-12"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-turn-13"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-14"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-15"
    },
    {
      "kind": "path",
      "lineId": "to-park-16"
    },
    {
      "kind": "path",
      "lineId": "to-park-17"
    },
    {
      "kind": "path",
      "lineId": "to-s-catch-18"
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
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-turn-22"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-23"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-24"
    },
    {
      "kind": "path",
      "lineId": "to-park-25"
    },
    {
      "kind": "path",
      "lineId": "to-park-26"
    },
    {
      "kind": "path",
      "lineId": "to-s-catch-27"
    },
    {
      "kind": "path",
      "lineId": "to-garden-in-28"
    },
    {
      "kind": "path",
      "lineId": "to-garden-29"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-30"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-turn-31"
    },
    {
      "kind": "path",
      "lineId": "to-wall-flower-32"
    },
    {
      "kind": "path",
      "lineId": "to-s-fire-33"
    },
    {
      "kind": "path",
      "lineId": "to-park-34"
    },
    {
      "kind": "path",
      "lineId": "to-park-35"
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
    "exportName": "qual-partner-stages",
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
      "N_PICK": [
        57.5,
        108,
        90
      ]
    },
    "pathEnds": {
      "to-s-catch-1": "S_CATCH",
      "to-n-pick-2": "N_PICK",
      "to-n-low-3": "N_LOW",
      "to-n-catch-4": "N_CATCH",
      "to-n-back-5": "N_BACK",
      "to-far-flower-turn-6": "FAR_FLOWER_TURN",
      "to-far-flower-7": "FAR_FLOWER",
      "to-n-fire-8": "N_FIRE",
      "to-s-catch-9": "S_CATCH",
      "to-garden-in-10": "GARDEN_IN",
      "to-garden-11": "GARDEN",
      "to-s-fire-12": "S_FIRE",
      "to-wall-flower-turn-13": "WALL_FLOWER_TURN",
      "to-wall-flower-14": "WALL_FLOWER",
      "to-s-fire-15": "S_FIRE",
      "to-park-16": "PARK",
      "to-park-17": "PARK",
      "to-s-catch-18": "S_CATCH",
      "to-garden-in-19": "GARDEN_IN",
      "to-garden-20": "GARDEN",
      "to-s-fire-21": "S_FIRE",
      "to-wall-flower-turn-22": "WALL_FLOWER_TURN",
      "to-wall-flower-23": "WALL_FLOWER",
      "to-s-fire-24": "S_FIRE",
      "to-park-25": "PARK",
      "to-park-26": "PARK",
      "to-s-catch-27": "S_CATCH",
      "to-garden-in-28": "GARDEN_IN",
      "to-garden-29": "GARDEN",
      "to-s-fire-30": "S_FIRE",
      "to-wall-flower-turn-31": "WALL_FLOWER_TURN",
      "to-wall-flower-32": "WALL_FLOWER",
      "to-s-fire-33": "S_FIRE",
      "to-park-34": "PARK",
      "to-park-35": "PARK"
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
        "lineId": "to-n-pick-2",
        "park": false
      },
      {
        "id": "p-5",
        "kind": "path",
        "lineId": "to-n-low-3",
        "park": false
      },
      {
        "id": "w-6",
        "kind": "firstOf",
        "label": "Fire the catch and the row",
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
        "id": "w-65",
        "kind": "firstOf",
        "label": "TIP 2?",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": [
              {
                "id": "w-49",
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
                "id": "p-50",
                "kind": "path",
                "lineId": "to-s-catch-27",
                "park": false
              },
              {
                "id": "w-51",
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
                "id": "p-52",
                "kind": "path",
                "lineId": "to-garden-in-28",
                "park": false
              },
              {
                "id": "p-53",
                "kind": "path",
                "lineId": "to-garden-29",
                "park": false
              },
              {
                "id": "w-54",
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
                "id": "p-55",
                "kind": "path",
                "lineId": "to-s-fire-30",
                "park": false
              },
              {
                "id": "w-56",
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
                "id": "w-64",
                "kind": "firstOf",
                "label": "TIP 3?",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": [
                      {
                        "id": "p-63",
                        "kind": "path",
                        "lineId": "to-park-35",
                        "park": true
                      }
                    ],
                    "label": "Yes: PARK"
                  },
                  {
                    "afterMs": 600,
                    "cards": [
                      {
                        "id": "p-57",
                        "kind": "path",
                        "lineId": "to-wall-flower-turn-31",
                        "park": false
                      },
                      {
                        "id": "p-58",
                        "kind": "path",
                        "lineId": "to-wall-flower-32",
                        "park": false
                      },
                      {
                        "id": "w-59",
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
                        "id": "p-60",
                        "kind": "path",
                        "lineId": "to-s-fire-33",
                        "park": false
                      },
                      {
                        "id": "w-61",
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
                        "id": "p-62",
                        "kind": "path",
                        "lineId": "to-park-34",
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
            "afterMs": 800,
            "cards": [
              {
                "id": "p-7",
                "kind": "path",
                "lineId": "to-n-catch-4",
                "park": false
              },
              {
                "id": "w-8",
                "kind": "firstOf",
                "label": "The rest of the row",
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
                "id": "p-9",
                "kind": "path",
                "lineId": "to-n-back-5",
                "park": false
              },
              {
                "id": "w-10",
                "kind": "firstOf",
                "label": "Fire the rest of the row",
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
                "id": "w-48",
                "kind": "firstOf",
                "label": "TIP 2? (B)",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": [
                      {
                        "id": "w-32",
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
                        "id": "p-33",
                        "kind": "path",
                        "lineId": "to-s-catch-18",
                        "park": false
                      },
                      {
                        "id": "w-34",
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
                        "id": "p-35",
                        "kind": "path",
                        "lineId": "to-garden-in-19",
                        "park": false
                      },
                      {
                        "id": "p-36",
                        "kind": "path",
                        "lineId": "to-garden-20",
                        "park": false
                      },
                      {
                        "id": "w-37",
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
                        "id": "p-38",
                        "kind": "path",
                        "lineId": "to-s-fire-21",
                        "park": false
                      },
                      {
                        "id": "w-39",
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
                        "id": "w-47",
                        "kind": "firstOf",
                        "label": "TIP 3? (B)",
                        "rows": [
                          {
                            "when": [
                              "Tip"
                            ],
                            "cards": [
                              {
                                "id": "p-46",
                                "kind": "path",
                                "lineId": "to-park-26",
                                "park": true
                              }
                            ],
                            "label": "Yes: PARK"
                          },
                          {
                            "afterMs": 600,
                            "cards": [
                              {
                                "id": "p-40",
                                "kind": "path",
                                "lineId": "to-wall-flower-turn-22",
                                "park": false
                              },
                              {
                                "id": "p-41",
                                "kind": "path",
                                "lineId": "to-wall-flower-23",
                                "park": false
                              },
                              {
                                "id": "w-42",
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
                                "id": "p-43",
                                "kind": "path",
                                "lineId": "to-s-fire-24",
                                "park": false
                              },
                              {
                                "id": "w-44",
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
                                "id": "p-45",
                                "kind": "path",
                                "lineId": "to-park-25",
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
                    "afterMs": 800,
                    "cards": [
                      {
                        "id": "p-11",
                        "kind": "path",
                        "lineId": "to-far-flower-turn-6",
                        "park": false
                      },
                      {
                        "id": "p-12",
                        "kind": "path",
                        "lineId": "to-far-flower-7",
                        "park": false
                      },
                      {
                        "id": "w-13",
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
                        "id": "p-14",
                        "kind": "path",
                        "lineId": "to-n-fire-8",
                        "park": false
                      },
                      {
                        "id": "w-15",
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
                        "id": "w-16",
                        "kind": "firstOf",
                        "label": "TIP 2 settles (C)",
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
                        "id": "p-17",
                        "kind": "path",
                        "lineId": "to-s-catch-9",
                        "park": false
                      },
                      {
                        "id": "w-18",
                        "kind": "firstOf",
                        "label": "Fire TIP 2's spill (C)",
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
                        "id": "p-19",
                        "kind": "path",
                        "lineId": "to-garden-in-10",
                        "park": false
                      },
                      {
                        "id": "p-20",
                        "kind": "path",
                        "lineId": "to-garden-11",
                        "park": false
                      },
                      {
                        "id": "w-21",
                        "kind": "firstOf",
                        "label": "The GARDEN (C)",
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
                        "id": "p-22",
                        "kind": "path",
                        "lineId": "to-s-fire-12",
                        "park": false
                      },
                      {
                        "id": "w-23",
                        "kind": "firstOf",
                        "label": "Fire the GARDEN (C)",
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
                        "id": "w-31",
                        "kind": "firstOf",
                        "label": "TIP 3? (C)",
                        "rows": [
                          {
                            "when": [
                              "Tip"
                            ],
                            "cards": [
                              {
                                "id": "p-30",
                                "kind": "path",
                                "lineId": "to-park-17",
                                "park": true
                              }
                            ],
                            "label": "Yes: PARK"
                          },
                          {
                            "afterMs": 600,
                            "cards": [
                              {
                                "id": "p-24",
                                "kind": "path",
                                "lineId": "to-wall-flower-turn-13",
                                "park": false
                              },
                              {
                                "id": "p-25",
                                "kind": "path",
                                "lineId": "to-wall-flower-14",
                                "park": false
                              },
                              {
                                "id": "w-26",
                                "kind": "firstOf",
                                "label": "The wall FLOWER (C)",
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
                                "lineId": "to-s-fire-15",
                                "park": false
                              },
                              {
                                "id": "w-28",
                                "kind": "firstOf",
                                "label": "Fire the wall FLOWER (TIP 3) (C)",
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
                                "id": "p-29",
                                "kind": "path",
                                "lineId": "to-park-16",
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
            ],
            "label": "No: the rest of the row"
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}