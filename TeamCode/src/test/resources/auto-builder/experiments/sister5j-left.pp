{
  "startPoint": {
    "x": 59,
    "y": 133.69,
    "name": "START",
    "headingDeg": 270
  },
  "lines": [
    {
      "id": "to-far-flower-turn-1",
      "color": "#3cc8e4",
      "name": "START to FAR_FLOWER_TURN",
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
          "x": 59,
          "y": 118.34
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
                "degrees": 270
              }
            },
            {
              "startProgress": 0.3,
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
      "id": "to-far-flower-2",
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
      "id": "to-l-n-3",
      "color": "#3cc8e4",
      "name": "FAR_FLOWER to L_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 116
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
      "id": "to-l-turn-4",
      "color": "#3cc8e4",
      "name": "L_N to L_TURN",
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
      "id": "to-l-s-5",
      "color": "#3cc8e4",
      "name": "L_TURN to L_S",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 59,
        "y": 22
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-l-c-6",
      "color": "#3cc8e4",
      "name": "L_S to L_C",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 58,
        "y": 40
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-l-n4-7",
      "color": "#3cc8e4",
      "name": "L_C to L_N4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 114
      },
      "controlPoints": [
        {
          "x": 57.5,
          "y": 50
        },
        {
          "x": 57.5,
          "y": 104
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-l-n-8",
      "color": "#3cc8e4",
      "name": "L_N4 to L_N",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 116
      },
      "controlPoints": [],
      "heading": {
        "type": "linear",
        "startDeg": 90,
        "endDeg": 270
      }
    },
    {
      "id": "to-l-f5-9",
      "color": "#3cc8e4",
      "name": "L_N to L_F5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 26
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
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-l-t5-10",
      "color": "#3cc8e4",
      "name": "L_F5 to L_T5",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 16
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-l-t4-11",
      "color": "#3cc8e4",
      "name": "L_N4 to L_T4",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 55,
        "y": 128
      },
      "controlPoints": [],
      "heading": {
        "type": "constant",
        "degrees": 90
      }
    },
    {
      "id": "to-park-l-12",
      "color": "#3cc8e4",
      "name": "L_T4 to PARK_L",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 120
      },
      "controlPoints": [
        {
          "x": 30,
          "y": 128
        }
      ],
      "heading": {
        "type": "constant",
        "degrees": 270
      }
    },
    {
      "id": "to-park-l-13",
      "color": "#3cc8e4",
      "name": "L_N to PARK_L",
      "waitBeforeMs": 0,
      "waitAfterMs": 0,
      "waitBeforeName": "",
      "waitAfterName": "",
      "kind": "atomic",
      "endPoint": {
        "x": 10.5,
        "y": 120
      },
      "controlPoints": [
        {
          "x": 44,
          "y": 127
        },
        {
          "x": 24,
          "y": 127
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
      "lineId": "to-far-flower-turn-1"
    },
    {
      "kind": "path",
      "lineId": "to-far-flower-2"
    },
    {
      "kind": "path",
      "lineId": "to-l-n-3"
    },
    {
      "kind": "path",
      "lineId": "to-l-turn-4"
    },
    {
      "kind": "path",
      "lineId": "to-l-s-5"
    },
    {
      "kind": "path",
      "lineId": "to-l-c-6"
    },
    {
      "kind": "path",
      "lineId": "to-l-n4-7"
    },
    {
      "kind": "path",
      "lineId": "to-l-n-8"
    },
    {
      "kind": "path",
      "lineId": "to-l-f5-9"
    },
    {
      "kind": "path",
      "lineId": "to-l-t5-10"
    },
    {
      "kind": "path",
      "lineId": "to-l-t4-11"
    },
    {
      "kind": "path",
      "lineId": "to-park-l-12"
    },
    {
      "kind": "path",
      "lineId": "to-park-l-13"
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
    "exportName": "sister5j-left",
    "registry": {
      "actions": [
        "SpinUp",
        "StreamOn",
        "StreamOff",
        "LaunchAll"
      ],
      "conditions": [
        "Empty",
        "LeftCellUp",
        "Tip",
        "IntakeFull",
        "RightCellUp"
      ],
      "typicalS": {
        "SpinUp": 0.1,
        "StreamOn": 1.0,
        "StreamOff": 1.0,
        "LaunchAll": 2.0
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
      "L_N": [
        55,
        116,
        270
      ],
      "L_TURN": [
        57,
        36,
        90
      ],
      "L_S": [
        59,
        22,
        90
      ],
      "PARK_L": [
        10.5,
        120,
        270
      ],
      "L_F5": [
        55,
        26,
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
      "L_C": [
        58,
        40,
        90
      ],
      "L_N4": [
        55,
        114,
        90
      ],
      "L_T5": [
        55,
        16,
        270
      ],
      "L_T4": [
        55,
        128,
        90
      ]
    },
    "pathEnds": {
      "to-far-flower-turn-1": "FAR_FLOWER_TURN",
      "to-far-flower-2": "FAR_FLOWER",
      "to-l-n-3": "L_N",
      "to-l-turn-4": "L_TURN",
      "to-l-s-5": "L_S",
      "to-l-c-6": "L_C",
      "to-l-n4-7": "L_N4",
      "to-l-n-8": "L_N",
      "to-l-f5-9": "L_F5",
      "to-l-t5-10": "L_T5",
      "to-l-t4-11": "L_T4",
      "to-park-l-12": "PARK_L",
      "to-park-l-13": "PARK_L"
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
        "lineId": "to-far-flower-turn-1",
        "park": false
      },
      {
        "id": "p-3",
        "kind": "path",
        "lineId": "to-far-flower-2",
        "park": false
      },
      {
        "id": "w-4",
        "kind": "firstOf",
        "label": "Seated",
        "rows": [
          {
            "when": [
              "Empty"
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
        "id": "w-5",
        "kind": "firstOf",
        "label": "TIP 1 (R): the left CELL up",
        "rows": [
          {
            "when": [
              "LeftCellUp"
            ],
            "cards": []
          },
          {
            "afterMs": 15000,
            "cards": []
          }
        ]
      },
      {
        "id": "a-6",
        "kind": "action",
        "name": "StreamOn"
      },
      {
        "id": "w-7",
        "kind": "firstOf",
        "label": "Preloads and the far FLOWER's 4, streamed (TIP 2)",
        "rows": [
          {
            "when": [
              "Tip"
            ],
            "cards": []
          },
          {
            "afterMs": 2600,
            "cards": []
          }
        ]
      },
      {
        "id": "a-8",
        "kind": "action",
        "name": "StreamOff"
      },
      {
        "id": "p-9",
        "kind": "path",
        "lineId": "to-l-n-3",
        "park": false
      },
      {
        "id": "w-36",
        "kind": "firstOf",
        "label": "TIP 2 started",
        "rows": [
          {
            "when": [
              "RightCellUp"
            ],
            "cards": [
              {
                "id": "w-10",
                "kind": "firstOf",
                "label": "TIP 2's spill lands",
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
                "id": "p-11",
                "kind": "path",
                "lineId": "to-l-turn-4",
                "park": false
              },
              {
                "id": "p-12",
                "kind": "path",
                "lineId": "to-l-s-5",
                "park": false
              },
              {
                "id": "w-13",
                "kind": "firstOf",
                "label": "TIP 2's catch at the right CELL (TIP 3, with R)",
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
                "id": "w-14",
                "kind": "firstOf",
                "label": "TIP 3",
                "rows": [
                  {
                    "when": [
                      "LeftCellUp"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 6000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "w-15",
                "kind": "firstOf",
                "label": "TIP 3's spill lands",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 800,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-16",
                "kind": "path",
                "lineId": "to-l-c-6",
                "park": false
              },
              {
                "id": "w-17",
                "kind": "firstOf",
                "label": "Catch TIP 3's spill",
                "rows": [
                  {
                    "when": [
                      "IntakeFull"
                    ],
                    "cards": []
                  },
                  {
                    "afterMs": 2000,
                    "cards": []
                  }
                ]
              },
              {
                "id": "p-18",
                "kind": "path",
                "lineId": "to-l-n4-7",
                "park": false
              },
              {
                "id": "w-19",
                "kind": "firstOf",
                "label": "TIP 3's catch at the left CELL (TIP 4, with R)",
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
                "id": "w-34",
                "kind": "firstOf",
                "label": "TIP 4",
                "rows": [
                  {
                    "when": [
                      "Tip"
                    ],
                    "cards": [
                      {
                        "id": "p-20",
                        "kind": "path",
                        "lineId": "to-l-n-8",
                        "park": false
                      },
                      {
                        "id": "w-22",
                        "kind": "firstOf",
                        "label": "Catch TIP 4's spill",
                        "rows": [
                          {
                            "when": [
                              "IntakeFull"
                            ],
                            "cards": []
                          },
                          {
                            "afterMs": 1200,
                            "cards": []
                          }
                        ]
                      },
                      {
                        "id": "p-23",
                        "kind": "path",
                        "lineId": "to-l-f5-9",
                        "park": false
                      },
                      {
                        "id": "w-24",
                        "kind": "firstOf",
                        "label": "TIP 4's catch at the right CELL (TIP 5, with R)",
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
                        "id": "p-25",
                        "kind": "path",
                        "lineId": "to-l-t5-10",
                        "park": false
                      },
                      {
                        "id": "w-26",
                        "kind": "firstOf",
                        "label": "Top-up off the floor",
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
                        "id": "w-28",
                        "kind": "firstOf",
                        "label": "Right CELL still up?",
                        "rows": [
                          {
                            "when": [
                              "RightCellUp"
                            ],
                            "cards": [
                              {
                                "id": "w-27",
                                "kind": "firstOf",
                                "label": "The top-up at the right CELL (TIP 5)",
                                "rows": [
                                  {
                                    "when": [
                                      "Tip"
                                    ],
                                    "cards": []
                                  },
                                  {
                                    "afterMs": 4000,
                                    "cards": []
                                  }
                                ],
                                "alongside": "LaunchAll"
                              }
                            ]
                          },
                          {
                            "afterMs": 50,
                            "cards": [],
                            "label": "No: the TIP came, hold the top-up"
                          }
                        ]
                      }
                    ]
                  },
                  {
                    "afterMs": 2500,
                    "cards": [
                      {
                        "id": "p-29",
                        "kind": "path",
                        "lineId": "to-l-t4-11",
                        "park": false
                      },
                      {
                        "id": "w-30",
                        "kind": "firstOf",
                        "label": "TIP 4 short: off the floor",
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
                        "id": "w-32",
                        "kind": "firstOf",
                        "label": "Left CELL still up?",
                        "rows": [
                          {
                            "when": [
                              "LeftCellUp"
                            ],
                            "cards": [
                              {
                                "id": "w-31",
                                "kind": "firstOf",
                                "label": "The top-up at the left CELL (TIP 4)",
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
                              }
                            ]
                          },
                          {
                            "afterMs": 50,
                            "cards": [],
                            "label": "No: the TIP came, hold the top-up"
                          }
                        ]
                      },
                      {
                        "id": "p-33",
                        "kind": "path",
                        "lineId": "to-park-l-12",
                        "park": true
                      }
                    ],
                    "label": "No TIP 4: top it up and park"
                  }
                ]
              }
            ]
          },
          {
            "afterMs": 4500,
            "cards": [
              {
                "id": "p-35",
                "kind": "path",
                "lineId": "to-park-l-13",
                "park": true
              }
            ],
            "label": "No TIP 2: park"
          }
        ]
      }
    ]
  },
  "version": "1.5.0"
}